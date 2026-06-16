package com.powerreliability.common.engine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 新政合规规则引擎 (5.3.1)
 * 内置标准化、不可篡改的政策规则库，所有规则判定全程留痕
 */
@Slf4j
@Component
public class PolicyRuleEngine {

    // ========== 频次管控规则 ==========
    /** 年停电上限 */
    private static final int MAX_OUTAGE_PER_YEAR = 5;
    /** 60天停电上限 */
    private static final int MAX_OUTAGE_PER_60_DAYS = 3;

    // ========== 指标阈值规则 ==========
    /** SAIDI阈值(分钟) */
    public static final double SAIDI_THRESHOLD = 60.0;
    /** SAIFI阈值(次/户) */
    public static final double SAIFI_THRESHOLD = 0.5;

    // ========== 豁免场景规则 ==========
    public enum ExemptScenario {
        FORCE_MAJEURE(1, "不可抗力-恶劣天气"),
        THIRD_PARTY(2, "第三方外力破坏"),
        USER_FAULT(3, "用户产权故障"),
        OTHER(4, "其他合规豁免");

        public final int code;
        public final String desc;
        ExemptScenario(int code, String desc) { this.code = code; this.desc = desc; }
        public static ExemptScenario fromCode(int code) {
            for (ExemptScenario e : values()) if (e.code == code) return e;
            return OTHER;
        }
    }

    // ========== 核心判定方法 ==========

    /** 频次合规检查 */
    public ComplianceResult checkFrequencyCompliance(long yearCount, long days60Count) {
        ComplianceResult result = new ComplianceResult();
        result.setYearCount(yearCount);
        result.setDays60Count(days60Count);
        result.setYearExceeded(yearCount >= MAX_OUTAGE_PER_YEAR);
        result.setDays60Exceeded(days60Count >= MAX_OUTAGE_PER_60_DAYS);
        result.setPassed(!result.isYearExceeded() && !result.isDays60Exceeded());
        result.setCheckTime(LocalDateTime.now());
        return result;
    }

    /** 豁免判定 */
    public ExemptVerdict judgeExemption(int exemptType, String evidence) {
        ExemptVerdict verdict = new ExemptVerdict();
        ExemptScenario scenario = ExemptScenario.fromCode(exemptType);
        verdict.setExemptType(exemptType);
        verdict.setExemptScenario(scenario);
        verdict.setHasEvidence(evidence != null && !evidence.isBlank());
        // 豁免判定规则
        verdict.setApproved(verdict.isHasEvidence());
        verdict.setVerdictTime(LocalDateTime.now());
        return verdict;
    }

    /** 指标阈值检查 */
    public ThresholdResult checkThreshold(String indexName, double value, double threshold) {
        ThresholdResult result = new ThresholdResult();
        result.setIndexName(indexName);
        result.setActualValue(value);
        result.setThreshold(threshold);
        result.setExceeded(value > threshold);
        result.setDeviationRate(threshold > 0 ?
            BigDecimal.valueOf((value - threshold) / threshold * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0);
        return result;
    }

    // ========== 结果模型 ==========
    @Data
    public static class ComplianceResult {
        private long yearCount;
        private long days60Count;
        private boolean yearExceeded;
        private boolean days60Exceeded;
        private boolean passed;
        private LocalDateTime checkTime;
    }

    @Data
    public static class ExemptVerdict {
        private int exemptType;
        private ExemptScenario exemptScenario;
        private boolean hasEvidence;
        private boolean approved;
        private LocalDateTime verdictTime;
    }

    @Data
    public static class ThresholdResult {
        private String indexName;
        private double actualValue;
        private double threshold;
        private boolean exceeded;
        private double deviationRate;
    }
}
