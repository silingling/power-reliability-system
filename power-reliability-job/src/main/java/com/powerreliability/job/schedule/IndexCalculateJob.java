package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.mapper.ConsumerMapper;
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
import java.util.List;

/**
 * 每日凌晨自动计算 SAIDI/SAIFI 等可靠性指标
 */
@Slf4j
@Component
public class IndexCalculateJob {

    @Autowired
    private OutageEventMapper outageEventMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private ReliabilityIndexMapper reliabilityIndexMapper;

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
                log.info("[IndexCalculateJob] 昨日无可统计停电事件，跳过计算");
                return;
            }

            // 2. 总用户数
            Long totalUsers = consumerMapper.selectCount(new LambdaQueryWrapper<>());
            if (totalUsers == null || totalUsers == 0) {
                log.warn("[IndexCalculateJob] consumer 表无数据，无法计算指标");
                totalUsers = 1L;
            }

            // 3. 聚合计算
            double totalOutageMinutes = 0;
            long totalOutageCount = events.size();

            for (OutageEvent event : events) {
                LocalDateTime start = event.getOutageStartTime();
                LocalDateTime end = event.getOutageEndTime();
                int minutes = 0;
                if (start != null && end != null) {
                    minutes = (int) Duration.between(start, end).toMinutes();
                }
                int affected = (event.getAffectedConsumerCount() != null)
                        ? event.getAffectedConsumerCount() : 0;

                totalOutageMinutes += (double) minutes * affected;
            }

            // SAIDI = Σ(停电持续时间 × 影响用户数) / 总用户数
            double saidi = totalOutageMinutes / totalUsers;
            // SAIFI = 停电总次数 / 总用户数
            double saifi = (double) totalOutageCount / totalUsers;
            // CAIDI = SAIDI / SAIFI
            double caidi = (saifi > 0) ? saidi / saifi : 0;
            // ASAI = (1440 - SAIDI) / 1440 * 100
            double asai = ((1440.0 - saidi) / 1440.0) * 100;
            if (asai < 0) asai = 0;
            if (asai > 100) asai = 100;

            // 4. 插入 reliability_index 表
            ReliabilityIndex index = new ReliabilityIndex();
            index.setStatType(0L);
            index.setTargetId(0L);
            index.setPeriod(0);
            index.setStatDate(yesterday);
            index.setSaidi(round(saidi, 2));
            index.setSaifi(round(saifi, 4));
            index.setCaidi(round(caidi, 2));
            index.setAsai(round(asai, 4));
            index.setAsidi(round(100 - asai, 4));
            index.setCreateTime(LocalDateTime.now());

            reliabilityIndexMapper.insert(index);

            log.info("[IndexCalculateJob] 指标计算完成: SAIDI={}, SAIFI={}, CAIDI={}, ASAI={}%",
                    round(saidi, 2), round(saifi, 4), round(caidi, 2), round(asai, 4));
        } catch (Exception e) {
            log.error("[IndexCalculateJob] 指标计算异常", e);
        }
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
