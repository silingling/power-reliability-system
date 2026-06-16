package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardPerformance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardPerformanceMapper extends BaseMapper<DashboardPerformance> {

    /**
     * 获取指定周期班组排名
     */
    @Select("SELECT assessment_target_name, total_score, assessment_grade, " +
            "area_violation_rate, governance_closure_rate, " +
            "warning_disposal_rate, avg_repair_time, outage_compliance_rate " +
            "FROM performance_assessment WHERE is_deleted = 0 " +
            "AND assessment_period = #{period} AND assessment_year = #{year} " +
            "AND assessment_month = #{month} AND assessment_target_type = 2 " +
            "ORDER BY total_score DESC")
    List<Map<String, Object>> selectTeamRanking(@Param("period") Integer period,
                                                 @Param("year") Integer year,
                                                 @Param("month") Integer month);
}
