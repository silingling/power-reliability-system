package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.governance.entity.FrequentOutageLedger;
import com.powerreliability.governance.entity.GovernanceEffect;
import com.powerreliability.governance.entity.GovernanceOrder;
import com.powerreliability.governance.mapper.FrequentOutageLedgerMapper;
import com.powerreliability.governance.mapper.GovernanceEffectMapper;
import com.powerreliability.governance.mapper.GovernanceOrderMapper;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 治理成效跟踪 Job
 *
 * 治理工单审核通过（status=3）后，在 30/60/90 天自动验证停电是否复燃：
 * 1. 查询近期通过审核的工单
 * 2. 检查 30/60/90 天间隔的停电数据
 * 3. 对比治理前的基线数据，计算停电减少率
 * 4. 生成 GovernanceEffect 记录
 * 5. 若停电超过治理前水平则标记为复燃，更新台账状态
 *
 * 设计文档：4.3.4 治理后效跟踪
 */
@Slf4j
@Component
public class GovernanceEffectTrackJob {

    @Autowired
    private GovernanceOrderMapper governanceOrderMapper;

    @Autowired
    private GovernanceEffectMapper governanceEffectMapper;

    @Autowired
    private OutageEventMapper outageEventMapper;

    @Autowired
    private FrequentOutageLedgerMapper frequentOutageLedgerMapper;

    /** 跟踪间隔天数 */
    private static final int[] TRACK_INTERVALS = {30, 60, 90};

    /** 复燃判定阈值：后效停电次数超过基线的此倍数即判定为复燃 */
    private static final BigDecimal REBOUND_THRESHOLD = BigDecimal.valueOf(0.8);

    /**
     * 每日凌晨 4 点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void execute() {
        log.info("[GovernanceEffectTrackJob] 开始治理成效跟踪，时间: {}", LocalDateTime.now());

        try {
            // 1. 查询最近 90 天内审核通过（status=3）的治理工单
            LocalDateTime threshold = LocalDateTime.now().minusDays(90);
            LambdaQueryWrapper<GovernanceOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(GovernanceOrder::getStatus, 2);
            orderWrapper.ge(GovernanceOrder::getReviewTime, threshold);
            List<GovernanceOrder> orders = governanceOrderMapper.selectList(orderWrapper);

            log.info("[GovernanceEffectTrackJob] 待跟踪的治理工单数: {}", orders.size());

            for (GovernanceOrder order : orders) {
                processGovernanceOrder(order);
            }

            log.info("[GovernanceEffectTrackJob] 治理成效跟踪完成，共处理 {} 条工单", orders.size());
        } catch (Exception e) {
            log.error("[GovernanceEffectTrackJob] 治理成效跟踪异常", e);
        }
    }

    /**
     * 处理单个治理工单的成效跟踪
     */
    private void processGovernanceOrder(GovernanceOrder order) {
        log.debug("[GovernanceEffectTrackJob] 处理工单: orderNo={}, id={}, reviewTime={}",
                order.getOrderNo(), order.getId(), order.getReviewTime());

        LocalDate reviewDate = order.getReviewTime() != null
                ? order.getReviewTime().toLocalDate()
                : LocalDate.now();

        try {
            // 查找关联的停电事件，获取 areaId
            Long areaId = findAreaIdByOrder(order);
            if (areaId == null) {
                log.warn("[GovernanceEffectTrackJob] 工单 {} 无法确定关联区域，跳过", order.getOrderNo());
                return;
            }

            // 获取治理前的基线数据（从 FrequentOutageLedger 同一区域的停电记录统计）
            PreGovernanceBaseline baseline = loadPreGovernanceBaseline(order, areaId);
            if (baseline == null) {
                log.warn("[GovernanceEffectTrackJob] 工单 {} 无法获取治理前基线数据，跳过", order.getOrderNo());
                return;
            }

            log.info("[GovernanceEffectTrackJob] 工单 {} 区域 areaId={}，基线: 停电{}次, 总影响{}户",
                    order.getOrderNo(), areaId, baseline.outageCount, baseline.totalAffectedUsers);

            // 对 30/60/90 天间隔逐一检查
            boolean anyRebound = false;

            for (int intervalDays : TRACK_INTERVALS) {
                try {
                    boolean tracked = processInterval(order, areaId, baseline, reviewDate, intervalDays);
                    if (tracked) {
                        anyRebound = true;
                    }
                } catch (Exception e) {
                    log.error("[GovernanceEffectTrackJob] 工单 {} 处理 {} 天间隔异常",
                            order.getOrderNo(), intervalDays, e);
                }
            }

            // 如果所有间隔都完成且没有复燃，且 90 天已完成 -> 更新台账状态为已完成
            if (!anyRebound && hasAllIntervalsTracked(order.getId())) {
                updateGovernanceStatus(order, "completed");
                log.info("[GovernanceEffectTrackJob] 工单 {} 治理成效全部跟踪完成，无复燃", order.getOrderNo());
            }

        } catch (Exception e) {
            log.error("[GovernanceEffectTrackJob] 处理工单 {} 异常", order.getOrderNo(), e);
        }
    }

    /**
     * 处理某一间隔（30/60/90 天）的成效检查
     *
     * @return true 如果判定为复燃
     */
    private boolean processInterval(GovernanceOrder order, Long areaId,
                                     PreGovernanceBaseline baseline,
                                     LocalDate reviewDate, int intervalDays) {
        // 计算本次间隔的统计时间：从审核通过日期算起 + intervalDays 天
        LocalDate effectDate = reviewDate.plusDays(intervalDays);

        // 如果统计时间还没到，跳过
        if (effectDate.isAfter(LocalDate.now())) {
            return false;
        }

        // 检查是否已存在该间隔的治理成效记录
        if (existsEffectForInterval(order.getId(), reviewDate, intervalDays)) {
            log.debug("[GovernanceEffectTrackJob] 工单 {} 在 {} 天已有成效记录，跳过", order.getOrderNo(), intervalDays);
            return false;
        }

        // 统计间隔时间范围内的停电事件
        // 统计窗口：从 reviewDate 到 effectDate（审核通过后到间隔日）
        LocalDateTime startTime = reviewDate.atStartOfDay();
        LocalDateTime endTime = effectDate.plusDays(1).atStartOfDay();

        List<OutageEvent> intervalEvents = queryOutageByAreaAndTimeRange(areaId, startTime, endTime);

        int postOutageCount = intervalEvents.size();
        int postTotalAffectedUsers = intervalEvents.stream()
                .mapToInt(e -> e.getAffectedConsumerCount() != null ? e.getAffectedConsumerCount() : 0)
                .sum();

        log.info("[GovernanceEffectTrackJob] 工单 {} {}天间隔: 停电{}次, 影响{}户",
                order.getOrderNo(), intervalDays, postOutageCount, postTotalAffectedUsers);

        // 计算停电减少率
        BigDecimal reductionRate = calculateReductionRate(baseline.outageCount, postOutageCount);

        // 计算影响用户减少数
        int userReduction = Math.max(0, baseline.totalAffectedUsers - postTotalAffectedUsers);

        // 计算成效评分
        int effectScore = calculateEffectScore(reductionRate);

        // 构建治理后描述
        String afterMeasure = String.format(
                "治理后%d天内共发生停电%d次，影响用户%d户，停电减少率%.1f%%",
                intervalDays, postOutageCount, postTotalAffectedUsers, reductionRate);

        // 创建治理成效记录
        GovernanceEffect effect = new GovernanceEffect();
        effect.setOrderId(order.getId());
        effect.setEffectDescription(buildEffectDescription(order, intervalDays, postOutageCount,
                baseline.outageCount, reductionRate));
        effect.setOutageReductionRate(reductionRate);
        effect.setAffectedUserReduction(userReduction);
        effect.setBeforeMeasure(baseline.toDescription());
        effect.setAfterMeasure(afterMeasure);
        effect.setEffectScore(effectScore);
        effect.setEffectTime(effectDate);
        governanceEffectMapper.insert(effect);

        log.info("[GovernanceEffectTrackJob] 工单 {} {}天成效记录已创建，减少率={}%，评分={}",
                order.getOrderNo(), intervalDays, reductionRate, effectScore);

        // 判定是否复燃：停电次数超过基线 80%
        boolean isRebound = postOutageCount > 0
                && BigDecimal.valueOf(postOutageCount)
                .compareTo(BigDecimal.valueOf(baseline.outageCount).multiply(REBOUND_THRESHOLD)) >= 0;

        if (isRebound) {
            log.warn("[GovernanceEffectTrackJob] 工单 {} 在{}天间隔出现复燃！" +
                            "后效停电{}次(基线{}次), 减少率={}%",
                    order.getOrderNo(), intervalDays, postOutageCount,
                    baseline.outageCount, reductionRate);

            // 更新台账状态为需整改
            updateGovernanceStatus(order, "rebounded");
        }

        return isRebound;
    }

    /**
     * 通过工单关联信息查找区域ID
     */
    private Long findAreaIdByOrder(GovernanceOrder order) {
        // 优先通过 eventId 查找 OutageEvent
        if (order.getEventId() != null && !order.getEventId().isEmpty()) {
            LambdaQueryWrapper<OutageEvent> eventWrapper = new LambdaQueryWrapper<>();
            eventWrapper.eq(OutageEvent::getEventNo, order.getEventId());
            OutageEvent event = outageEventMapper.selectOne(eventWrapper);
            if (event != null && event.getAreaId() != null) {
                return event.getAreaId();
            }
        }
        return null;
    }

    /**
     * 加载治理前的基线数据
     */
    private PreGovernanceBaseline loadPreGovernanceBaseline(GovernanceOrder order, Long areaId) {
        // 从 FrequentOutageLedger 获取该工单关联的停电记录作为基线
        // 先尝试通过 eventId 精确匹配
        if (order.getEventId() != null && !order.getEventId().isEmpty()) {
            LambdaQueryWrapper<FrequentOutageLedger> ledgerWrapper = new LambdaQueryWrapper<>();
            ledgerWrapper.eq(FrequentOutageLedger::getEventId, order.getEventId());
            List<FrequentOutageLedger> ledgers = frequentOutageLedgerMapper.selectList(ledgerWrapper);

            if (!ledgers.isEmpty()) {
                int totalOutages = ledgers.size();
                int totalAffected = ledgers.stream()
                        .mapToInt(l -> l.getAffectedUsers() != null ? l.getAffectedUsers() : 0)
                        .sum();
                return new PreGovernanceBaseline(totalOutages, totalAffected, ledgers);
            }
        }

        // 如果精确匹配不到，通过 areaId 反查 FrequentOutageLedger
        // 先通过 OutageEvent 获取 areaCode
        LambdaQueryWrapper<OutageEvent> areaEventWrapper = new LambdaQueryWrapper<>();
        areaEventWrapper.eq(OutageEvent::getAreaId, areaId);
        areaEventWrapper.last("LIMIT 1");
        OutageEvent areaEvent = outageEventMapper.selectOne(areaEventWrapper);

        if (areaEvent != null && areaEvent.getAreaCode() != null) {
            LambdaQueryWrapper<FrequentOutageLedger> areaLedgerWrapper = new LambdaQueryWrapper<>();
            areaLedgerWrapper.eq(FrequentOutageLedger::getAreaCode, areaEvent.getAreaCode());
            List<FrequentOutageLedger> areaLedgers = frequentOutageLedgerMapper.selectList(areaLedgerWrapper);

            if (!areaLedgers.isEmpty()) {
                int totalOutages = areaLedgers.size();
                int totalAffected = areaLedgers.stream()
                        .mapToInt(l -> l.getAffectedUsers() != null ? l.getAffectedUsers() : 0)
                        .sum();
                return new PreGovernanceBaseline(totalOutages, totalAffected, areaLedgers);
            }
        }

        return null;
    }

    /**
     * 按区域和时间范围查询停电事件
     */
    private List<OutageEvent> queryOutageByAreaAndTimeRange(Long areaId,
                                                             LocalDateTime startTime,
                                                             LocalDateTime endTime) {
        LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutageEvent::getAreaId, areaId);
        wrapper.ge(OutageEvent::getOutageStartTime, startTime);
        wrapper.lt(OutageEvent::getOutageStartTime, endTime);
        wrapper.orderByAsc(OutageEvent::getOutageStartTime);
        return outageEventMapper.selectList(wrapper);
    }

    /**
     * 检查指定工单在指定间隔天数是否已有成效记录
     * 使用 Java LocalDate 计算，不依赖 MySQL DATEDIFF
     */
    private boolean existsEffectForInterval(Long orderId, LocalDate reviewDate, int intervalDays) {
        LambdaQueryWrapper<GovernanceEffect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GovernanceEffect::getOrderId, orderId);
        wrapper.eq(GovernanceEffect::getEffectTime, reviewDate.plusDays(intervalDays));
        return governanceEffectMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查 30/60/90 天间隔是否已全部跟踪完成
     */
    private boolean hasAllIntervalsTracked(Long orderId) {
        LambdaQueryWrapper<GovernanceEffect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GovernanceEffect::getOrderId, orderId);
        Long count = governanceEffectMapper.selectCount(wrapper);
        // 工单自创建可能不满 90 天，统计实际可执行的间隔数
        return count != null && count >= TRACK_INTERVALS.length;
    }

    /**
     * 计算停电减少率
     */
    private BigDecimal calculateReductionRate(int baselineCount, int postCount) {
        if (baselineCount <= 0) {
            return BigDecimal.valueOf(100);
        }
        BigDecimal reduction = BigDecimal.valueOf(baselineCount - postCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(baselineCount), 2, RoundingMode.HALF_UP);
        return reduction.max(BigDecimal.ZERO);
    }

    /**
     * 根据减少率计算成效评分
     */
    private int calculateEffectScore(BigDecimal reductionRate) {
        if (reductionRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return 5;
        } else if (reductionRate.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return 4;
        } else if (reductionRate.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return 3;
        } else if (reductionRate.compareTo(BigDecimal.valueOf(20)) >= 0) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 构建成效描述
     */
    private String buildEffectDescription(GovernanceOrder order, int intervalDays,
                                           int postCount, int preCount, BigDecimal reductionRate) {
        return String.format(
                "治理工单「%s」%d天成效跟踪：治理前停电%d次，治理后%d天内停电%d次，停电减少率%.1f%%。",
                order.getTitle(), intervalDays, preCount, intervalDays, postCount, reductionRate);
    }

    /**
     * 更新频繁停滞台账的治理状态
     */
    private void updateGovernanceStatus(GovernanceOrder order, String status) {
        if (order.getEventId() == null || order.getEventId().isEmpty()) {
            return;
        }

        LambdaQueryWrapper<FrequentOutageLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FrequentOutageLedger::getEventId, order.getEventId());
        List<FrequentOutageLedger> ledgers = frequentOutageLedgerMapper.selectList(wrapper);

        for (FrequentOutageLedger ledger : ledgers) {
            ledger.setGovernanceStatus(status);
            frequentOutageLedgerMapper.updateById(ledger);
            log.info("[GovernanceEffectTrackJob] 台账 {} 治理状态更新为: {}", ledger.getId(), status);
        }
    }

    /**
     * 治理前基线数据
     */
    private static class PreGovernanceBaseline {
        final int outageCount;
        final int totalAffectedUsers;

        PreGovernanceBaseline(int outageCount, int totalAffectedUsers) {
            this.outageCount = outageCount;
            this.totalAffectedUsers = totalAffectedUsers;
        }

        String toDescription() {
            return String.format("治理前：停电%d次，影响用户%d户", outageCount, totalAffectedUsers);
        }
    }
}
