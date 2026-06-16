package com.powerreliability.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.dashboard.entity.DashboardFrequentOutageLedger;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardFrequentOutageLedgerMapper extends BaseMapper<DashboardFrequentOutageLedger> {

    /**
     * 按风险等级统计
     */
    @Select("SELECT " +
            "SUM(CASE WHEN risk_level = 1 THEN 1 ELSE 0 END) AS high_risk_count, " +
            "SUM(CASE WHEN risk_level = 2 THEN 1 ELSE 0 END) AS medium_risk_count, " +
            "SUM(CASE WHEN risk_level = 3 THEN 1 ELSE 0 END) AS low_risk_count, " +
            "SUM(CASE WHEN governance_status = 0 THEN 1 ELSE 0 END) AS ungoverned_count " +
            "FROM frequent_outage_ledger WHERE is_deleted = 0")
    Map<String, Object> riskSummary();
}
