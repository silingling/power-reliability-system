package com.powerreliability.warning.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.warning.entity.RiskPrediction;
import com.powerreliability.warning.mapper.RiskPredictionMapper;
import com.powerreliability.warning.service.RiskPredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 隐患预判服务实现 - 多因素风险评分引擎
 */
@Slf4j
@Service
public class RiskPredictionServiceImpl extends ServiceImpl<RiskPredictionMapper, RiskPrediction>
        implements RiskPredictionService {

    @Override
    public int autoPredict() {
        log.info("开始执行多因素风险评分预判...");
        List<RiskPrediction> predictions = new ArrayList<>();

        // ========== 模拟数据源：在实际项目中，这些数据应从台账模块查询 ==========
        // 模拟设备列表（应来自 power-reliability-ledger 的 Equipment）
        List<MockDevice> devices = mockDeviceData();
        // 模拟天气因子（应来自外部气象接口）
        BigDecimal weatherFactor = mockWeatherFactor();

        for (MockDevice device : devices) {
            // 1. 设备老化评分 (0-30)
            BigDecimal ageScore = calcAgeScore(device.commissionYears);
            // 2. 历史故障评分 (0-25)
            BigDecimal faultScore = calcFaultScore(device.faultCount);
            // 3. 负荷率评分 (0-25)
            BigDecimal loadScore = calcLoadScore(device.loadRate);
            // 4. 天气影响评分 (0-20)
            BigDecimal weatherScore = calcWeatherScore(weatherFactor);

            BigDecimal totalScore = ageScore.add(faultScore).add(loadScore).add(weatherScore);
            totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);

            // 低于20分无风险，跳过
            if (totalScore.compareTo(BigDecimal.valueOf(20)) < 0) {
                continue;
            }

            int riskLevel;
            if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
                riskLevel = 4; // 紧急
            } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
                riskLevel = 3; // 高
            } else if (totalScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
                riskLevel = 2; // 中
            } else {
                riskLevel = 1; // 低
            }

            RiskPrediction p = new RiskPrediction();
            p.setEquipmentId(device.equipmentId);
            p.setLineId(device.lineId);
            p.setAreaId(device.areaId);
            p.setPredictionType(5); // 综合
            p.setRiskLevel(riskLevel);
            p.setRiskScore(totalScore);
            p.setEquipmentAge(device.commissionYears);
            p.setHistoricalFaultCount(device.faultCount);
            p.setLoadRate(device.loadRate);
            p.setWeatherFactor(weatherFactor);
            p.setPredictionDesc(String.format(
                    "设备[%s]综合风险评分%.2f：老化%.0f分 + 故障%.0f分 + 负荷%.0f分 + 天气%.0f分",
                    device.equipmentName, totalScore, ageScore, faultScore, loadScore, weatherScore));
            p.setPredictionDetail(String.format(
                    "{\"ageScore\":%.2f,\"faultScore\":%.2f,\"loadScore\":%.2f,\"weatherScore\":%.2f}",
                    ageScore, faultScore, loadScore, weatherScore));
            p.setSuggestion(buildSuggestion(riskLevel, device));
            p.setPredictionTime(LocalDateTime.now());
            p.setStatus(0); // 待处理
            predictions.add(p);
        }

        if (!predictions.isEmpty()) {
            saveBatch(predictions);
        }

        log.info("多因素风险评分预判完成，生成 {} 条预判记录", predictions.size());
        return predictions.size();
    }

    // ==================== 评分算法 ====================

    /** 设备老化评分：运行年限越长，评分越高 */
    private BigDecimal calcAgeScore(Integer years) {
        if (years == null || years < 1) return BigDecimal.ZERO;
        if (years >= 25) return BigDecimal.valueOf(30);
        if (years >= 20) return BigDecimal.valueOf(25);
        if (years >= 15) return BigDecimal.valueOf(20);
        if (years >= 10) return BigDecimal.valueOf(15);
        if (years >= 5) return BigDecimal.valueOf(10);
        return BigDecimal.valueOf(5);
    }

    /** 历史故障评分 */
    private BigDecimal calcFaultScore(Integer faultCount) {
        if (faultCount == null || faultCount <= 0) return BigDecimal.ZERO;
        if (faultCount >= 10) return BigDecimal.valueOf(25);
        if (faultCount >= 5) return BigDecimal.valueOf(20);
        if (faultCount >= 3) return BigDecimal.valueOf(15);
        if (faultCount >= 1) return BigDecimal.valueOf(10);
        return BigDecimal.ZERO;
    }

    /** 负荷率评分 */
    private BigDecimal calcLoadScore(BigDecimal loadRate) {
        if (loadRate == null) return BigDecimal.ZERO;
        if (loadRate.compareTo(BigDecimal.valueOf(100)) >= 0) return BigDecimal.valueOf(25);
        if (loadRate.compareTo(BigDecimal.valueOf(90)) >= 0) return BigDecimal.valueOf(20);
        if (loadRate.compareTo(BigDecimal.valueOf(80)) >= 0) return BigDecimal.valueOf(15);
        if (loadRate.compareTo(BigDecimal.valueOf(70)) >= 0) return BigDecimal.valueOf(10);
        if (loadRate.compareTo(BigDecimal.valueOf(60)) >= 0) return BigDecimal.valueOf(5);
        return BigDecimal.ZERO;
    }

    /** 天气影响评分 */
    private BigDecimal calcWeatherScore(BigDecimal weatherFactor) {
        if (weatherFactor == null) return BigDecimal.ZERO;
        BigDecimal score = weatherFactor.multiply(BigDecimal.valueOf(20))
                .setScale(2, RoundingMode.HALF_UP);
        return score.min(BigDecimal.valueOf(20));
    }

    /** 根据风险等级生成建议措施 */
    private String buildSuggestion(int riskLevel, MockDevice device) {
        switch (riskLevel) {
            case 4:
                return String.format("【紧急】%s 设备风险极高，建议立即停电检修，安排专项排查", device.equipmentName);
            case 3:
                return String.format("【高】%s 高隐患，建议7日内安排检修，加强巡检频率", device.equipmentName);
            case 2:
                return String.format("【中】%s 存在中等风险，建议列入月度检修计划", device.equipmentName);
            case 1:
                return String.format("【低】%s 存在一般性风险，建议加强日常巡检", device.equipmentName);
            default:
                return "建议加强日常巡检";
        }
    }

    // ==================== 模拟数据 ====================

    private List<MockDevice> mockDeviceData() {
        List<MockDevice> list = new ArrayList<>();
        // 模拟10台设备
        list.add(new MockDevice(1L, "变压器A-01", 1L, 1L, 18, 8, new BigDecimal("85.5")));
        list.add(new MockDevice(2L, "变压器A-02", 1L, 1L, 22, 12, new BigDecimal("92.3")));
        list.add(new MockDevice(3L, "变压器B-01", 2L, 2L, 12, 3, new BigDecimal("72.1")));
        list.add(new MockDevice(4L, "变压器C-01", 3L, 3L, 6, 1, new BigDecimal("55.0")));
        list.add(new MockDevice(5L, "变压器C-02", 3L, 3L, 15, 6, new BigDecimal("78.6")));
        list.add(new MockDevice(6L, "开关柜D-01", 4L, 4L, 10, 4, new BigDecimal("88.8")));
        list.add(new MockDevice(7L, "开关柜D-02", 4L, 4L, 8, 2, new BigDecimal("65.4")));
        list.add(new MockDevice(8L, "柱上开关E-01", 5L, 5L, 20, 15, new BigDecimal("95.0")));
        list.add(new MockDevice(9L, "柱上开关E-02", 5L, 5L, 3, 0, new BigDecimal("45.2")));
        list.add(new MockDevice(10L, "电缆分支箱F-01", 6L, 6L, 16, 5, new BigDecimal("81.3")));
        return list;
    }

    private BigDecimal mockWeatherFactor() {
        // 模拟天气影响因子：0.0-1.0，实际应从气象接口获取
        return BigDecimal.valueOf(RandomUtil.randomDouble(0.1, 0.6))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** 本地模拟数据结构 */
    private record MockDevice(Long equipmentId, String equipmentName, Long lineId, Long areaId,
                              Integer commissionYears, Integer faultCount, BigDecimal loadRate) {
    }
}
