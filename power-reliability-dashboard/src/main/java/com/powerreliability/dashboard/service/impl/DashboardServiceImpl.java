package com.powerreliability.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.dashboard.entity.DashboardTrArea;
import com.powerreliability.dashboard.mapper.*;
import com.powerreliability.dashboard.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private DashboardTrAreaMapper trAreaMapper;

    @Autowired
    private DashboardOutageEventMapper outageEventMapper;

    @Autowired
    private DashboardRepairOrderMapper repairOrderMapper;

    @Autowired
    private DashboardGovernanceOrderMapper governanceOrderMapper;

    @Autowired
    private DashboardReliabilityIndexMapper reliabilityIndexMapper;

    @Autowired
    private DashboardRiskWarningMapper riskWarningMapper;

    @Autowired
    private DashboardFrequentOutageLedgerMapper frequentOutageLedgerMapper;

    @Autowired
    private DashboardPerformanceMapper performanceMapper;

    @Override
    public Map<String, Object> overview() {
        // 台区总数及状态统计
        List<DashboardTrArea> allAreas = trAreaMapper.selectList(
                new LambdaQueryWrapper<DashboardTrArea>());
        long totalAreas = allAreas.size();
        long normalAreas = allAreas.stream().filter(a -> a.getAreaStatus() != null && a.getAreaStatus() == 1).count();
        long offlineAreas = allAreas.stream().filter(a -> a.getAreaStatus() != null && a.getAreaStatus() == 0).count();

        // 频繁停电超标/风险/隐患统计
        Map<String, Object> riskSummary = frequentOutageLedgerMapper.riskSummary();

        // 待处置预警数
        long pendingWarnings = riskWarningMapper.selectCount(
                new LambdaQueryWrapper<com.powerreliability.dashboard.entity.DashboardRiskWarning>()
                        .lt(com.powerreliability.dashboard.entity.DashboardRiskWarning::getOrderStatus, 2));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAreas", totalAreas);
        result.put("normalAreas", normalAreas);
        result.put("offlineAreas", offlineAreas);
        result.put("remainingAreas", totalAreas - normalAreas);
        result.put("highRiskCount", riskSummary.getOrDefault("high_risk_count", 0));
        result.put("mediumRiskCount", riskSummary.getOrDefault("medium_risk_count", 0));
        result.put("lowRiskCount", riskSummary.getOrDefault("low_risk_count", 0));
        result.put("ungovernedCount", riskSummary.getOrDefault("ungoverned_count", 0));
        result.put("pendingWarnings", pendingWarnings);
        return result;
    }

    @Override
    public Map<String, Object> outageStat(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDateTime endTime = now;

        switch (period) {
            case "today":
                startTime = now.toLocalDate().atStartOfDay();
                break;
            case "week":
                startTime = now.toLocalDate().atStartOfDay().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                break;
            case "month":
                startTime = now.toLocalDate().atStartOfDay().withDayOfMonth(1);
                break;
            default:
                startTime = now.toLocalDate().atStartOfDay();
        }

        // 总停电次数和影响
        Map<String, Object> summary = outageEventMapper.summaryBetween(startTime, endTime);
        // 按类型统计
        List<Map<String, Object>> byType = outageEventMapper.countByTypeBetween(startTime, endTime);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", period);
        result.put("totalCount", summary.getOrDefault("total_count", 0));
        result.put("totalAffected", summary.getOrDefault("total_affected", 0));
        result.put("totalDuration", summary.getOrDefault("total_duration", 0));
        result.put("byType", byType);
        return result;
    }

    @Override
    public List<Map<String, Object>> indexTrend() {
        // 最近12个月的月度趋势
        LocalDate startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        return reliabilityIndexMapper.selectMonthlyTrend(startDate);
    }

    @Override
    public Map<String, Object> repairStat() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.toLocalDate().atStartOfDay().withDayOfMonth(1);
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.toLocalDate().atStartOfDay()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        Map<String, Object> monthStat = repairOrderMapper.repairStatsBetween(monthStart, now);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", monthStat.getOrDefault("total_orders", 0));
        result.put("completedOrders", monthStat.getOrDefault("completed_orders", 0));
        result.put("avgRecoveryMinutes", monthStat.getOrDefault("avg_recovery_minutes", 0));
        result.put("completionRate",
                NumberToPercent(((Number) monthStat.getOrDefault("completed_orders", 0)).doubleValue(),
                        ((Number) monthStat.getOrDefault("total_orders", 1)).doubleValue()));
        return result;
    }

    @Override
    public Map<String, Object> governanceProgress() {
        return governanceOrderMapper.governanceProgress();
    }

    @Override
    public List<Map<String, Object>> areaHeatmap() {
        return trAreaMapper.selectAllForHeatmap();
    }

    @Override
    public List<Map<String, Object>> warningList() {
        return riskWarningMapper.selectPendingWarnings();
    }

    @Override
    public List<Map<String, Object>> ranking() {
        LocalDate now = LocalDate.now();
        return performanceMapper.selectTeamRanking(1, now.getYear(), now.getMonthValue());
    }

    /** 辅助方法：数字转百分比字符串 */
    private String NumberToPercent(double numerator, double denominator) {
        if (denominator == 0) return "0.0%";
        return String.format("%.1f%%", numerator / denominator * 100);
    }
}
