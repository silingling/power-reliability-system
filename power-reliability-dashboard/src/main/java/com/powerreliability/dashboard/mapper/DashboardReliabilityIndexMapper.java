package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardReliabilityIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardReliabilityIndexMapper extends BaseMapper<DashboardReliabilityIndex> {

    /**
     * 获取月度SAIDI/SAIFI趋势
     */
    @Select("SELECT stat_start_date, stat_end_date, saidi, saifi, " +
            "total_outage_count, total_affected_consumers, total_outage_minutes " +
            "FROM reliability_index WHERE is_deleted = 0 " +
            "AND stat_type = 1 AND stat_period = 3 " +
            "AND stat_start_date >= #{startDate} " +
            "ORDER BY stat_start_date ASC")
    List<Map<String, Object>> selectMonthlyTrend(@Param("startDate") LocalDate startDate);
}
