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

    /**
     * 生成月度可靠性报表数据
     *
     * @param year  年份
     * @param month 月份
     * @return 报表数据列表
     */
    java.util.List<java.util.Map<String, Object>> exportMonthlyReport(Integer year, Integer month);

    /**
     * 同期对比分析
     *
     * @param statType   统计类型
     * @param targetId   目标ID
     * @param baseYear   基准年份
     * @param compareYear 对比年份
     * @return 对比分析数据
     */
    java.util.Map<String, Object> comparePeriods(Long statType, Long targetId, Integer baseYear, Integer compareYear);
}
