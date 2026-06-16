package com.powerreliability.index.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.index.entity.ReliabilityIndex;

import java.time.LocalDate;

/**
 * 可靠性指标统计服务
 */
public interface ReliabilityIndexService extends IService<ReliabilityIndex> {

    /**
     * 计算可靠性指标
     *
     * @param statType  统计类型
     * @param targetId  目标ID
     * @param period    统计周期 0-日, 1-月, 2-年
     * @param start     开始日期
     * @param end       结束日期
     * @return 生成的指标记录ID
     */
    Long calculateIndex(Long statType, Long targetId, Integer period, LocalDate start, LocalDate end);
}
