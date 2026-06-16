package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardTrArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardTrAreaMapper extends BaseMapper<DashboardTrArea> {

    /**
     * 按供电所统计合区数量和状态分布
     */
    @Select("SELECT power_supply_station AS station, " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN area_status = 1 THEN 1 ELSE 0 END) AS normal_count, " +
            "SUM(CASE WHEN area_status = 0 THEN 1 ELSE 0 END) AS offline_count, " +
            "SUM(total_consumers) AS total_consumers " +
            "FROM tr_area WHERE is_deleted = 0 " +
            "GROUP BY power_supply_station")
    List<Map<String, Object>> groupByStation();

    /**
     * 查询所有台区经纬度信息用于热力图
     */
    @Select("SELECT id, area_code, area_name, longitude, latitude, area_status, " +
            "total_consumers, power_supply_station, maintenance_team " +
            "FROM tr_area WHERE is_deleted = 0 AND longitude IS NOT NULL AND latitude IS NOT NULL")
    List<Map<String, Object>> selectAllForHeatmap();
}
