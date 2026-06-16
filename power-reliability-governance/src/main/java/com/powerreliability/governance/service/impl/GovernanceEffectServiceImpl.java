package com.powerreliability.governance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.governance.entity.GovernanceEffect;
import com.powerreliability.governance.mapper.GovernanceEffectMapper;
import com.powerreliability.governance.service.GovernanceEffectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 治理成效跟踪服务实现
 */
@Slf4j
@Service
public class GovernanceEffectServiceImpl
        extends ServiceImpl<GovernanceEffectMapper, GovernanceEffect>
        implements GovernanceEffectService {

    @Override
    public Map<String, Object> compareBeforeAfter(Long ledgerId) {
        LambdaQueryWrapper<GovernanceEffect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GovernanceEffect::getOrderId, ledgerId)
                .orderByDesc(GovernanceEffect::getEffectTime);

        List<GovernanceEffect> effects = list(wrapper);
        if (effects.isEmpty()) {
            log.warn("未找到治理成效记录, ledgerId={}", ledgerId);
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ledgerId", ledgerId);
        result.put("recordCount", effects.size());

        // 计算平均值
        double totalReductionRate = 0;
        int totalUserReduction = 0;
        int totalScore = 0;
        int validCount = 0;

        for (GovernanceEffect effect : effects) {
            if (effect.getOutageReductionRate() != null) {
                totalReductionRate += effect.getOutageReductionRate().doubleValue();
            }
            if (effect.getAffectedUserReduction() != null) {
                totalUserReduction += effect.getAffectedUserReduction();
            }
            if (effect.getEffectScore() != null) {
                totalScore += effect.getEffectScore();
            }
            validCount++;
        }

        if (validCount > 0) {
            result.put("avgOutageReductionRate", BigDecimal.valueOf(totalReductionRate / validCount));
            result.put("avgAffectedUserReduction", totalUserReduction / validCount);
            result.put("avgEffectScore", totalScore / validCount);
        }

        // 对比治理前后的情况
        GovernanceEffect latest = effects.get(0);
        result.put("beforeMeasure", latest.getBeforeMeasure());
        result.put("afterMeasure", latest.getAfterMeasure());
        result.put("effectDescription", latest.getEffectDescription());

        log.info("治理前后对比分析完成, ledgerId={}, totalRecords={}", ledgerId, effects.size());
        return result;
    }

    @Override
    public Map<String, Object> getEffectivenessRate(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<GovernanceEffect> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(GovernanceEffect::getCreateTime, startTime)
                .le(GovernanceEffect::getCreateTime, endTime);

        List<GovernanceEffect> effects = list(wrapper);
        int total = effects.size();

        int improvedCount = 0;       // 效果改善（outageReductionRate > 0）
        int significantCount = 0;    // 显著改善（outageReductionRate >= 50% 或 effectScore >= 80）
        int stableCount = 0;         // 无明显变化
        int regressionCount = 0;     // 反弹（outageReductionRate < 0）

        for (GovernanceEffect effect : effects) {
            BigDecimal rate = effect.getOutageReductionRate();
            Integer score = effect.getEffectScore();

            if (rate != null) {
                double r = rate.doubleValue();
                if (r >= 50) {
                    significantCount++;
                    improvedCount++;
                } else if (r > 0) {
                    improvedCount++;
                } else if (r == 0) {
                    stableCount++;
                } else {
                    regressionCount++;
                }
            }
            if (score != null && score >= 80 && rate != null && rate.doubleValue() < 50) {
                significantCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalGovernanceCount", total);
        result.put("improvedCount", improvedCount);
        result.put("significantCount", significantCount);
        result.put("stableCount", stableCount);
        result.put("regressionCount", regressionCount);

        if (total > 0) {
            result.put("effectivenessRate", BigDecimal.valueOf(improvedCount * 100.0 / total)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("significantRate", BigDecimal.valueOf(significantCount * 100.0 / total)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            result.put("regressionRate", BigDecimal.valueOf(regressionCount * 100.0 / total)
                    .setScale(2, java.math.RoundingMode.HALF_UP));
        }

        log.info("治理有效率统计完成, 总量={}, 有效率={}%砥", total,
                total > 0 ? improvedCount * 100.0 / total : 0);
        return result;
    }
}
