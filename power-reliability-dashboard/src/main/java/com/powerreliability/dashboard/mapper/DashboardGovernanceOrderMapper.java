package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardGovernanceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardGovernanceOrderMapper extends BaseMapper<DashboardGovernanceOrder> {

    /**
     * 统计治理进度
     */
    @Select("SELECT " +
            "COUNT(*) AS total, " +
            "SUM(CASE WHEN order_status = 0 THEN 1 ELSE 0 END) AS pending_count, " +
            "SUM(CASE WHEN order_status = 1 THEN 1 ELSE 0 END) AS in_progress_count, " +
            "SUM(CASE WHEN order_status = 2 THEN 1 ELSE 0 END) AS review_count, " +
            "SUM(CASE WHEN order_status >= 3 THEN 1 ELSE 0 END) AS completed_count " +
            "FROM governance_order WHERE is_deleted = 0")
    Map<String, Object> governanceProgress();

    /**
     * 按供电所统计治理情况
     */
    @Select("SELECT g.area_name, g.area_code, g.order_status, g.responsible_team " +
            "FROM governance_order g WHERE g.is_deleted = 0 " +
            "ORDER BY g.create_time DESC")
    List<Map<String, Object>> selectAllOrders();
}
