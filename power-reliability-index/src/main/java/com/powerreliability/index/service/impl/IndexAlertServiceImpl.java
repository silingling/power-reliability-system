package com.powerreliability.index.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.index.entity.IndexAlert;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.IndexAlertMapper;
import com.powerreliability.index.service.IndexAlertService;
import com.powerreliability.index.service.ReliabilityIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 指标异常预警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexAlertServiceImpl
        extends ServiceImpl<IndexAlertMapper, IndexAlert>
        implements IndexAlertService {

    private final ReliabilityIndexService reliabilityIndexService;

    /** SAIFI 阈值 */
    @Value("${index.alert.saifi-threshold:3.0}")
    private double saifiThreshold;

    /** SAIDI 阈值 */
    @Value("${index.alert.saidi-threshold:5.0}")
    private double saidiThreshold;

    /** ASAI 阈值（低于此值预警） */
    @Value("${index.alert.asai-threshold:99.90}")
    private double asaiThreshold;

    /** 偏差率预警阈值 */
    @Value("${index.alert.deviation-threshold:20.0}")
    private double deviationThreshold;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkThresholds() {
        log.info("开始检查指标阈值, SAIFI阈值={}, SAIDI阈值={}, ASAI阈值={}",
                saifiThreshold, saidiThreshold, asaiThreshold);

        // 获取最新的指标记录
        List<ReliabilityIndex> indices = reliabilityIndexService.list();
        int alertCount = 0;

        for (ReliabilityIndex index : indices) {
            // 忽略已存在预警的指标（去重）
            LambdaQueryWrapper<IndexAlert> existsWrapper = new LambdaQueryWrapper<>();
            existsWrapper.eq(IndexAlert::getIndexId, index.getId());
            if (count(existsWrapper) > 0) {
                continue;
            }

            // 检查 SAIFI
            if (index.getSaifi() != null && index.getSaifi() > saifiThreshold) {
                createAlert(index, "SAIFI", index.getSaifi(), saifiThreshold, 
                        "系统平均停电频率超阈值");
                alertCount++;
            }

            // 检查 SAIDI
            if (index.getSaidi() != null && index.getSaidi() > saidiThreshold) {
                createAlert(index, "SAIDI", index.getSaidi(), saidiThreshold,
                        "系统平均停电持续时间超阈值");
                alertCount++;
            }

            // 检查 ASAI
            if (index.getAsai() != null && index.getAsai() < asaiThreshold) {
                createAlert(index, "ASAI", index.getAsai(), asaiThreshold,
                        "平均供电可用率低于阈值");
                alertCount++;
            }
        }

        log.info("阈值检查完成, 生成预警: {} 条", alertCount);
        return alertCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlert(Long id, String measures) {
        IndexAlert alert = getById(id);
        if (alert == null) {
            throw new IllegalArgumentException("预警记录不存在: " + id);
        }
        if (alert.getStatus() != null && alert.getStatus() == 1) {
            throw new IllegalArgumentException("预警已处理: " + id);
        }

        alert.setStatus(1);
        alert.setHandleMeasures(measures);
        alert.setHandleTime(LocalDateTime.now());
        updateById(alert);

        log.info("预警 {} 已处理, 措施: {}", id, measures);
    }

    private void createAlert(ReliabilityIndex index, String indexName, 
                              Double value, Double threshold, String alertType) {
        IndexAlert alert = new IndexAlert();
        alert.setIndexId(index.getId());
        alert.setStatType(index.getStatType());
        alert.setTargetId(index.getTargetId());
        alert.setAlertType(alertType);
        alert.setAlertLevel(value > threshold * 1.5 ? "high" : "medium");
        alert.setIndexName(indexName);
        alert.setIndexValue(value);
        alert.setThresholdValue(threshold);
        alert.setDeviationRate((value - threshold) / threshold * 100);
        alert.setAlertTime(LocalDateTime.now());
        alert.setStatus(0);
        save(alert);

        log.warn("指标预警生成: {}={}, 阈值={}, 偏差率={}%", 
                indexName, value, threshold, alert.getDeviationRate());
    }
}
