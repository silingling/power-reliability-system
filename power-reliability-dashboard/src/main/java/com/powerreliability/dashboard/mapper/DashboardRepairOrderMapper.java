package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardRepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardRepairOrderMapper extends BaseMapper<DashboardRepairOrder> {

    /**
     * 统计抢修完成率和平均复电时长
     */
    @Select("SELECT " +
            "COUNT(*) AS total_orders, " +
            "SUM(CASE WHEN order_status >= 3 THEN 1 ELSE 0 END) AS completed_orders, " +
            "COALESCE(AVG(TIMESTAMPDIFF(MINUTE, dispatch_time, repair_complete_time)), 0) AS avg_recovery_minutes " +
            "FROM repair_order WHERE is_deleted = 0 " +
            "AND dispatch_time >= #{startTime} AND dispatch_time <= #{endTime}")
    Map<String, Object> repairStatsBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 按班组统计抢修排名
     */
    @Select("SELECT repair_team AS team_name, " +
            "COUNT(*) AS total_orders, " +
            "SUM(CASE WHEN order_status >= 3 THEN 1 ELSE 0 END) AS completed_orders, " +
            "COALESCE(AVG(TIMESTAMPDIFF(MINUTE, dispatch_time, repair_complete_time)), 0) AS avg_recovery_minutes, " +
            "COALESCE(SUM(CASE WHEN order_status >= 3 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(*), 0), 0) AS completion_rate " +
            "FROM repair_order WHERE is_deleted = 0 " +
            "AND dispatch_time >= #{startTime} AND dispatch_time <= #{endTime} " +
            "GROUP BY repair_team ORDER BY completion_rate DESC")
    List<Map<String, Object>> teamRankingBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);
}
