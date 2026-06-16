package com.powerreliability.common.engine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 低压停电智能归因引擎 (5.3.2)
 * 八大类标准诱因自动归因
 */
@Slf4j
@Component
public class OutageCauseEngine {

    public enum CauseCategory {
        EQUIPMENT_AGING(1, "设备老化缺陷", "设备超期服役、绝缘老化"),
        LINE_HAZARD(2, "低压线路隐患", "线路老化、接头松动"),
        CONNECTION_OVERHEAT(3, "接头发热故障", "接点氧化、接触电阻大"),
        LOAD_OVERLOAD(4, "负荷过载", "负荷超容、配变过载"),
        PHASE_IMBALANCE(5, "三相不平衡", "三相负荷分配不均"),
        WEATHER(6, "恶劣天气影响", "雷雨、大风、冰雪"),
        EXTERNAL_DAMAGE(7, "外力施工破坏", "施工挖断、车辆撞杆"),
        MAINTENANCE(8, "运维巡检不到位", "消缺不及时、巡视漏项");

        public final int code;
        public final String name;
        public final String typicalDesc;
        CauseCategory(int code, String name, String typicalDesc) {
            this.code = code; this.name = name; this.typicalDesc = typicalDesc;
        }
        public static CauseCategory fromCode(int code) {
            for (CauseCategory c : values()) if (c.code == code) return c;
            return MAINTENANCE;
        }
    }

    /** 归因权重配置 */
    private static final Map<String, Map<Integer, Double>> WEIGHT_MAP = new HashMap<>();
    static {
        // 设备老化因子 → 各诱因权重
        Map<Integer, Double> ageWeights = new HashMap<>();
        ageWeights.put(1, 0.70); ageWeights.put(2, 0.10); ageWeights.put(6, 0.10);
        WEIGHT_MAP.put("equipment_age", ageWeights);

        // 负荷因子 → 权重
        Map<Integer, Double> loadWeights = new HashMap<>();
        loadWeights.put(4, 0.60); loadWeights.put(5, 0.30);
        WEIGHT_MAP.put("load_rate", loadWeights);

        // 历史故障因子
        Map<Integer, Double> faultWeights = new HashMap<>();
        faultWeights.put(1, 0.25); faultWeights.put(2, 0.25); faultWeights.put(3, 0.20); faultWeights.put(8, 0.15);
        WEIGHT_MAP.put("fault_count", faultWeights);

        // 天气因子
        Map<Integer, Double> weatherWeights = new HashMap<>();
        weatherWeights.put(6, 0.80); weatherWeights.put(7, 0.10);
        WEIGHT_MAP.put("weather", weatherWeights);
    }

    /**
     * 多因子加权归因分析
     * @param equipmentAge 设备运行年限
     * @param faultCount 历史故障次数
     * @param loadRate 负荷率(%)
     * @param weatherFactor 天气影响因子(0-1)
     * @return CauseResult 包含归因结果
     */
    public CauseResult analyze(Integer equipmentAge, Integer faultCount, Double loadRate, Double weatherFactor) {
        CauseResult result = new CauseResult();
        Map<Integer, Double> scores = new HashMap<>();
        for (int i = 1; i <= 8; i++) scores.put(i, 0.0);

        // 设备老化维度
        if (equipmentAge != null) {
            double ageFactor = Math.min(1.0, equipmentAge / 30.0);
            applyWeights(scores, "equipment_age", ageFactor);
        }

        // 负荷率维度
        if (loadRate != null) {
            double loadFactor = Math.min(1.0, loadRate / 100.0);
            applyWeights(scores, "load_rate", loadFactor);
        }

        // 历史故障维度
        if (faultCount != null) {
            double faultFactor = Math.min(1.0, faultCount / 20.0);
            applyWeights(scores, "fault_count", faultFactor);
        }

        // 天气维度
        if (weatherFactor != null) {
            applyWeights(scores, "weather", weatherFactor);
        }

        // 确定主要归因
        Map.Entry<Integer, Double> primary = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry(8, 0.0));

        CauseCategory category = CauseCategory.fromCode(primary.getKey());
        result.setPrimaryCauseType(primary.getKey());
        result.setPrimaryCauseName(category.name);
        result.setPrimaryCauseDesc(category.typicalDesc);
        result.setConfidence(BigDecimal.valueOf(primary.getValue() * 100).setScale(2, BigDecimal.ROUND_HALF_UP));
        result.setScoreDetails(scores);

        // 排序诱因
        List<CauseFactor> factors = new ArrayList<>();
        scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> {
                    CauseCategory c = CauseCategory.fromCode(e.getKey());
                    factors.add(new CauseFactor(c.code, c.name, BigDecimal.valueOf(e.getValue() * 100).setScale(2, BigDecimal.ROUND_HALF_UP)));
                });
        result.setTop3Causes(factors);

        log.info("[OutageCauseEngine] 归因完成: {} (置信度{}%)", category.name, result.getConfidence());
        return result;
    }

    private void applyWeights(Map<Integer, Double> scores, String factor, double factorValue) {
        Map<Integer, Double> weights = WEIGHT_MAP.get(factor);
        if (weights == null) return;
        for (Map.Entry<Integer, Double> w : weights.entrySet()) {
            scores.merge(w.getKey(), w.getValue() * factorValue, Double::sum);
        }
    }

    @Data
    public static class CauseResult {
        private Integer primaryCauseType;
        private String primaryCauseName;
        private String primaryCauseDesc;
        private BigDecimal confidence;
        private Map<Integer, Double> scoreDetails;
        private List<CauseFactor> top3Causes;
    }

    @Data
    public static class CauseFactor {
        private int causeType;
        private String causeName;
        private BigDecimal score;
        public CauseFactor(int causeType, String causeName, BigDecimal score) {
            this.causeType = causeType; this.causeName = causeName; this.score = score;
        }
    }
}
