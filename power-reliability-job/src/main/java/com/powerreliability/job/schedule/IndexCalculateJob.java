package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.mapper.ConsumerMapper;
import com.powerreliability.ledger.mapper.TrAreaMapper;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日凌晨自动计算 SAIDI/SAIFI 等可靠性指标
 * 同时计算：全局指标（statType=0） + 分台区指标（statType=1, targetId=areaId）
 */
@Slf4j
@Component
public class IndexCalculateJob {

    @Autowired
    private OutageEventMapper outageEventMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private TrAreaMapper trAreaMapper;

    @Autowired
    private ReliabilityIndexMapper reliabilityIndexMapper;

    /** 统计类型：全局 */
    private static final long STAT_TYPE_GLOBAL = 0L;
    /** 统计类型：分台区 */
    private static final long STAT_TYPE_AREA = 1L;

    /**
     * 每日凌晨 2:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("[IndexCalculateJob] 开始每日可靠性指标计算，时间: {}", LocalDateTime.now());

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime dayStart = yesterday.atStartOfDay();
            LocalDateTime dayEnd = yesterday.atTime(23, 59, 59);

            // 1. 查询昨日所有已闭环的非豁免停电事件
            LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(OutageEvent::getOutageEndTime, dayStart, dayEnd)
                    .eq(OutageEvent::getIsExempt, 0)
                    .eq(OutageEvent::getIsClosed, 1);
            List<OutageEvent> events = outageEventMapper.selectList(wrapper);

            if (events.isEmpty()) {
                log.info("[IndexCalculateJob] 昨日无可统计停电事件，仅计算基础指标");
                // 仍尝试插入一条零指标记录
            }

            // 2. 总用户数（全局）
            Long totalUsers = consumerMapper.selectCount(new LambdaQueryWrapper<>());
            if (totalUsers == null || totalUsers == 0) {
                log.warn("[IndexCalculateJob] consumer 表无数据，无法计算 SAIDI/SAIFI");
                totalUsers = 1L;
            }

            // ===== 全局指标（statType=0, targetId=0） =====
            calcAndSaveGlobal(yesterday, events, totalUsers);

            // ===== 分台区指标（statType=1, targetId=areaId） =====
            calcAndSavePerArea(yesterday, events);

            log.info("[IndexCalculateJob] 指标计算完成");
        } catch (Exception e) {
            log.error("[IndexCalculateJob] 指标计算异常", e);
        }
    }

    /** 计算全局指标 */
    private void calcAndSaveGlobal(LocalDate statDate, List<OutageEvent> events, long totalUsers) {
        double totalOutageMinutes = 0;
        long totalOutageCount = events.size();

        for (OutageEvent event : events) {
            int minutes = calcOutageMinutes(event);
            int affected = event.getAffectedConsumerCount() != null ? event.getAffectedConsumerCount() : 0;
            totalOutageMinutes += (double) minutes * affected;
        }

        double saidi = totalOutageMinutes / totalUsers;
        double saifi = (double) totalOutageCount / totalUsers;
        double caidi = (saifi > 0) ? saidi / saifi : 0;
        double asai = ((1440.0 - saidi) / 1440.0) * 100;
        if (asai < 0) asai = 0;
        if (asai > 100) asai = 100;

        ReliabilityIndex index = new ReliabilityIndex();
        index.setStatType(STAT_TYPE_GLOBAL);
        index.setTargetId(0L);
        index.setPeriod(0);
        index.setStatDate(statDate);
        index.setSaidi(round(saidi, 2));
        index.setSaifi(round(saifi, 4));
        index.setCaidi(round(caidi, 2));
        index.setAsai(round(asai, 4));
        index.setAsidi(round(100 - asai, 4));
        index.setEns(0.0);
        index.setAens(0.0);
        index.setCreateTime(LocalDateTime.now());
        reliabilityIndexMapper.insert(index);

        log.info("[IndexCalculateJob] 全局指标: SAIDI={}, SAIFI={}, CAIDI={}, ASAI={}%",
                index.getSaidi(), index.getSaifi(), index.getCaidi(), index.getAsai());
    }

    /** 按台区分组计算指标 */
    private void calcAndSavePerArea(LocalDate statDate, List<OutageEvent> events) {
        // 获取所有台区
        List<TrArea> areas = trAreaMapper.selectList(null);
        if (areas.isEmpty()) {
            log.warn("[IndexCalculateJob] 无台区数据，跳过分区指标计算");
            return;
        }

        // 按 area_code 分组停电事件
        Map<String, List<OutageEvent>> eventsByArea = new HashMap<>();
        for (OutageEvent event : events) {
            if (event.getAreaCode() == null) continue;
            eventsByArea.computeIfAbsent(event.getAreaCode(), k -> new ArrayList<>()).add(event);
        }

        // 获取分台区用户数
        Map<String, Long> usersByArea = new HashMap<>();
        for (TrArea area : areas) {
            LambdaQueryWrapper<Consumer> cw = new LambdaQueryWrapper<>();
            cw.eq(Consumer::getAreaCode, area.getAreaCode());
            long count = consumerMapper.selectCount(cw);
            usersByArea.put(area.getAreaCode(), count > 0 ? count : 1L);
        }

        int savedCount = 0;
        for (TrArea area : areas) {
            List<OutageEvent> areaEvents = eventsByArea.getOrDefault(area.getAreaCode(), Collections.emptyList());
            long areaUsers = usersByArea.get(area.getAreaCode());

            double totalOutageMinutes = 0;
            long totalOutageCount = areaEvents.size();

            for (OutageEvent event : areaEvents) {
                int minutes = calcOutageMinutes(event);
                int affected = event.getAffectedConsumerCount() != null ? event.getAffectedConsumerCount() : 0;
                totalOutageMinutes += (double) minutes * affected;
            }

            double saidi = totalOutageMinutes / areaUsers;
            double saifi = (double) totalOutageCount / areaUsers;
            double caidi = (saifi > 0) ? saidi / saifi : 0;
            double asai = ((1440.0 - saidi) / 1440.0) * 100;
            if (asai < 0) asai = 0;
            if (asai > 100) asai = 100;

            ReliabilityIndex index = new ReliabilityIndex();
            index.setStatType(STAT_TYPE_AREA);
            index.setTargetId(area.getId());
            index.setPeriod(0);
            index.setStatDate(statDate);
            index.setSaidi(round(saidi, 2));
            index.setSaifi(round(saifi, 4));
            index.setCaidi(round(caidi, 2));
            index.setAsai(round(asai, 4));
            index.setAsidi(round(100 - asai, 4));
            index.setCreateTime(LocalDateTime.now());
            reliabilityIndexMapper.insert(index);
            savedCount++;

            log.debug("[IndexCalculateJob] 台区[{}] SAIDI={}, SAIFI={}, ASAI={}%",
                    area.getAreaName(), index.getSaidi(), index.getSaifi(), index.getAsai());
        }

        log.info("[IndexCalculateJob] 分台区指标计算完成: {} 个台区, 停电区域 {} 个",
                savedCount, eventsByArea.keySet().size());
    }

    /** 计算停电分钟数 */
    private int calcOutageMinutes(OutageEvent event) {
        if (event.getOutageStartTime() != null && event.getOutageEndTime() != null) {
            return (int) Duration.between(event.getOutageStartTime(), event.getOutageEndTime()).toMinutes();
        }
        if (event.getOutageDuration() != null) {
            return event.getOutageDuration();
        }
        return 0;
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
