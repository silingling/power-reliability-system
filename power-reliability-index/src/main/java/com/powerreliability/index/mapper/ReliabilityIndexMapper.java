package com.powerreliability.index.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.index.entity.ReliabilityIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReliabilityIndexMapper extends BaseMapper<ReliabilityIndex> {

    /**
     * 按日期范围查询指标数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 指标数据列表
     */
    List<ReliabilityIndex> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按统计类型、日期范围和周期查询
     *
     * @param statType 统计类型
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param period    统计周期
     * @return 指标数据列表
     */
    List<ReliabilityIndex> selectByStatTypeAndDateRange(
            @Param("statType") Long statType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("period") Integer period);
}
