package com.powerreliability.governance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.governance.entity.FrequentOutageLedger;
import com.powerreliability.governance.mapper.FrequentOutageLedgerMapper;
import com.powerreliability.governance.service.FrequentOutageLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 频繁停电台账服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrequentOutageLedgerServiceImpl
        extends ServiceImpl<FrequentOutageLedgerMapper, FrequentOutageLedger>
        implements FrequentOutageLedgerService {

    /** 频繁停电判定阈值（次数） */
    @Value("${governance.frequent-outage.threshold-count:3}")
    private int thresholdCount;

    /** 频繁停电判定周期（天） */
    @Value("${governance.frequent-outage.threshold-days:30}")
    private int thresholdDays;

    /** 高风险停电时长阈值（分钟） */
    @Value("${governance.frequent-outage.high-risk-duration:120}")
    private int highRiskDuration;

    /** 低风险停电时长阈值（分钟） */
    @Value("${governance.frequent-outage.low-risk-duration:30}")
    private int lowRiskDuration;

    @Override
    public int autoScreen() {
        log.info("启动频繁停电自动筛查, 阈值: {}次/{}天", thresholdCount, thresholdDays);

        LocalDateTime since = LocalDateTime.now().minusDays(thresholdDays);

        // 1. 统计周期内各区域各线路停电次数
        LambdaQueryWrapper<FrequentOutageLedger> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.ge(FrequentOutageLedger::getOutageStartTime, since);

        List<FrequentOutageLedger> allRecords = list(countWrapper);

        // 2. 按 areaCode 分组统计停电次数
        java.util.Map<String, Long> areaCountMap = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getAreaCode() + "|" + (r.getAreaName() != null ? r.getAreaName() : ""),
                        java.util.stream.Collectors.counting()
                ));

        int violationCount = 0;

        // 3. 标记违规记录
        for (FrequentOutageLedger record : allRecords) {
            String key = record.getAreaCode() + "|" + (record.getAreaName() != null ? record.getAreaName() : "");
            long count = areaCountMap.getOrDefault(key, 0L);

            if (count >= thresholdCount) {
                // 判定为高风险
                if (!"high".equals(record.getRiskLevel())) {
                    record.setRiskLevel("high");
                    record.setGovernanceStatus("pending");
                    updateById(record);
                    violationCount++;
                }
            } else if (record.getOutageDuration() != null && record.getOutageDuration() >= highRiskDuration) {
                if (!"high".equals(record.getRiskLevel())) {
                    record.setRiskLevel("high");
                    record.setGovernanceStatus("pending");
                    updateById(record);
                    violationCount++;
                }
            } else if (record.getOutageDuration() != null && record.getOutageDuration() >= lowRiskDuration) {
                if (!"medium".equals(record.getRiskLevel())) {
                    record.setRiskLevel("medium");
                    record.setGovernanceStatus("pending");
                    updateById(record);
                    violationCount++;
                }
            }
        }

        log.info("自动筛查完成, 标记违规记录: {} 条", violationCount);
        return violationCount;
    }
}
