package com.powerreliability.index.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.index.service.ReliabilityIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 可靠性指标统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReliabilityIndexServiceImpl
        extends ServiceImpl<ReliabilityIndexMapper, ReliabilityIndex>
        implements ReliabilityIndexService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long calculateIndex(Long statType, Long targetId, Integer period, LocalDate start, LocalDate end) {
        log.info("开始计算可靠性指标 statType={}, targetId={}, period={}, range=[{}, {}]",
                statType, targetId, period, start, end);

        // 模拟指标计算逻辑
        ReliabilityIndex index = new ReliabilityIndex();
        index.setStatType(statType);
        index.setTargetId(targetId);
        index.setPeriod(period);
        index.setStatDate(start);

        // 示例：基于历史数据模拟计算各可靠性指标
        // 实际场景中从停电事件表聚合计算
        index.setAsai(99.95);
        index.setAsidi(0.05);
        index.setSaifi(1.25);
        index.setSaidi(2.30);
        index.setCaidi(1.84);
        index.setEns(1500.0);
        index.setAens(0.75);

        save(index);

        log.info("可靠性指标计算完成, id={}, SAIFI={}, SAIDI={}", index.getId(), index.getSaifi(), index.getSaidi());
        return index.getId();
    }
}
