package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardRiskWarning;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardRiskWarningMapper extends BaseMapper<DashboardRiskWarning> {

    /**
     * 获取待处置的预警清单
     */
    @Select("SELECT w.id, w.area_code, w.risk_type, w.risk_level, " +
            "w.warning_desc, w.warning_time, w.response_deadline, w.order_status, " +
            "a.area_name, a.power_supply_station " +
            "FROM risk_warning_order w " +
            "LEFT JOIN tr_area a ON w.area_id = a.id " +
            "WHERE w.is_deleted = 0 AND w.order_status < 2 " +
            "ORDER BY w.risk_level ASC, w.warning_time DESC " +
            "LIMIT 50")
    List<Map<String, Object>> selectPendingWarnings();
}
