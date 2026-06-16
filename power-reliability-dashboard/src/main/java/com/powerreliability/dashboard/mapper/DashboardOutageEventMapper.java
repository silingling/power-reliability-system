package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardOutageEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardOutageEventMapper extends BaseMapper<DashboardOutageEvent> {

    /**
     * 按停电类型统计指定时间范围内的停电数量
     */
    @Select("SELECT outage_type, COUNT(*) AS cnt " +
            "FROM outage_event WHERE is_deleted = 0 " +
            "AND outage_start_time >= #{startTime} AND outage_start_time <= #{endTime} " +
            "GROUP BY outage_type")
    List<Map<String, Object>> countByTypeBetween(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的总停电次数和影响用户数
     */
    @Select("SELECT COUNT(*) AS total_count, " +
            "COALESCE(SUM(affected_consumer_count), 0) AS total_affected, " +
            "COALESCE(SUM(outage_duration), 0) AS total_duration " +
            "FROM outage_event WHERE is_deleted = 0 " +
            "AND outage_start_time >= #{startTime} AND outage_start_time <= #{endTime} " +
            "AND is_exempt = 0")
    Map<String, Object> summaryBetween(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 按天统计停电次数趋势
     */
    @Select("SELECT DATE(outage_start_time) AS day, COUNT(*) AS cnt " +
            "FROM outage_event WHERE is_deleted = 0 " +
            "AND outage_start_time >= #{startTime} AND outage_start_time <= #{endTime} " +
            "GROUP BY DATE(outage_start_time) ORDER BY day")
    List<Map<String, Object>> trendByDayBetween(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
}
