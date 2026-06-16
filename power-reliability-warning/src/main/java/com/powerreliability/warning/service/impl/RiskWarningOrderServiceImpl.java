package com.powerreliability.warning.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.warning.entity.RiskPrediction;
import com.powerreliability.warning.entity.RiskWarningOrder;
import com.powerreliability.warning.mapper.RiskWarningOrderMapper;
import com.powerreliability.warning.service.RiskPredictionService;
import com.powerreliability.warning.service.RiskWarningOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningOrderServiceImpl extends ServiceImpl<RiskWarningOrderMapper, RiskWarningOrder>
        implements RiskWarningOrderService {

    private final RiskPredictionService riskPredictionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatchWarning() {
        log.info("开始根据预判记录自动生成预警工单...");

        // 查询所有待处理（status=0）且风险等级>=中（>=2）的预判记录
        List<RiskPrediction> predictions = riskPredictionService.lambdaQuery()
                .eq(RiskPrediction::getStatus, 0)
                .ge(RiskPrediction::getRiskLevel, 2)
                .list();

        if (predictions.isEmpty()) {
            log.info("没有待处理的预判记录");
            return 0;
        }

        List<RiskWarningOrder> orders = new ArrayList<>();
        for (RiskPrediction p : predictions) {
            RiskWarningOrder order = new RiskWarningOrder();
            order.setOrderNo(generateOrderNo());
            order.setPredictionId(p.getId());
            order.setWarningType(mapWarningType(p.getPredictionType()));
            order.setUrgencyLevel(mapUrgencyLevel(p.getRiskLevel()));
            order.setWarningTitle(String.format("【%s】风险%d级隐患预警",
                    getLevelLabel(p.getRiskLevel()), p.getRiskLevel()));
            order.setWarningContent(p.getPredictionDesc());
            order.setEquipmentId(p.getEquipmentId());
            order.setLineId(p.getLineId());
            order.setAreaId(p.getAreaId());
            order.setDispatchTarget("运维班组");
            order.setDispatchTime(LocalDateTime.now());
            order.setResponsiblePerson("待分配");
            order.setDisposeStatus(0); // 待处置
            orders.add(order);

            // 更新预判记录状态为已生成工单
            p.setStatus(1);
        }

        saveBatch(orders);
        riskPredictionService.updateBatchById(predictions);

        log.info("自动生成预警工单完成，共生成 {} 条工单", orders.size());
        return orders.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispose(Long id, String desc) {
        RiskWarningOrder order = getById(id);
        if (order == null) {
            log.warn("工单不存在: {}", id);
            return false;
        }
        if (order.getDisposeStatus() != 0) {
            log.warn("工单状态不是待处置，当前状态: {}", order.getDisposeStatus());
            return false;
        }
        order.setDisposeStatus(1); // 处置中
        order.setDisposeDesc(desc);
        order.setDisposePerson("当前操作人");
        order.setDisposeTime(LocalDateTime.now());
        order.setDisposeStatus(2); // 已处置
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long id, Integer result) {
        RiskWarningOrder order = getById(id);
        if (order == null) {
            log.warn("工单不存在: {}", id);
            return false;
        }
        if (order.getDisposeStatus() != 2) {
            log.warn("工单尚未处置，当前状态: {}", order.getDisposeStatus());
            return false;
        }
        order.setReviewResult(result);
        if (result == 1) {
            order.setReviewOpinion("复核通过，隐患已消除");
            order.setDisposeStatus(3); // 已复核
        } else {
            order.setReviewOpinion("复核不通过，需重新处置");
            order.setDisposeStatus(2); // 退回处置
        }
        order.setReviewPerson("当前复核人");
        order.setReviewTime(LocalDateTime.now());
        return updateById(order);
    }

    // ==================== 辅助方法 ====================

    private String generateOrderNo() {
        return "WARN" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
                + RandomUtil.randomNumbers(4);
    }

    private int mapWarningType(Integer predictType) {
        if (predictType == null) return 4;
        return switch (predictType) {
            case 1 -> 1;   // 设备预警
            case 2 -> 2;   // 线路预警
            case 3 -> 3;   // 台区预警
            default -> 4;  // 综合预警
        };
    }

    private int mapUrgencyLevel(Integer riskLevel) {
        if (riskLevel == null) return 1;
        return switch (riskLevel) {
            case 4 -> 3;   // 紧急
            case 3 -> 2;   // 重要
            default -> 1;  // 一般
        };
    }

    private String getLevelLabel(Integer riskLevel) {
        return switch (riskLevel) {
            case 4 -> "紧急";
            case 3 -> "高危";
            case 2 -> "中危";
            default -> "一般";
        };
    }
}
