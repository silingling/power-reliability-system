package com.powerreliability.job.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日凌晨自动计算 SAIDI/SAIFI 等可靠性指标
 */
@Slf4j
@Component
public class IndexCalculateJob {

    /**
     * 每日凌晨 2:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("[IndexCalculateJob] 开始每日可靠性指标计算，时间: {}", LocalDateTime.now());

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            // 实际调用 ReliabilityIndexService.calculateIndex() 聚合昨日停电数据
            log.info("[IndexCalculateJob] 开始计算日期: {} 的 SAIDI/SAIFI 指标", yesterday);

            // ===== 计算逻辑（伪代码，实际注入 Service 调用） =====
            // 1. 查询昨日所有非豁免停电事件
            // 2. 按区域/供电所/全维度聚合
            // 3. 计算 SAIDI = Σ(每次停电持续时间 × 影响用户数) / 总用户数
            // 4. 计算 SAIFI = 停电总次数 / 总用户数
            // 5. 计算 CAIDI = SAIDI / SAIFI
            // 6. 写入 reliability_index 表

            log.info("[IndexCalculateJob] 可靠性指标计算完成，时间: {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("[IndexCalculateJob] 指标计算异常", e);
        }
    }
}
