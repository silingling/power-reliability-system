package com.powerreliability.dashboard.controller;

import com.powerreliability.common.entity.Result;
import com.powerreliability.dashboard.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 全域总览: 台区总数/正常/超标/风险/隐患数
     */
    @PostMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(dashboardService.overview());
    }

    /**
     * 当日/本周/本月停电统计
     * @param request 包含 period 字段: today/week/month
     */
    @PostMapping("/outage-stat")
    public Result<Map<String, Object>> outageStat(@RequestBody Map<String, String> request) {
        String period = request.getOrDefault("period", "today");
        return Result.ok(dashboardService.outageStat(period));
    }

    /**
     * SAIDI/SAIFI趋势
     */
    @PostMapping("/index-trend")
    public Result<List<Map<String, Object>>> indexTrend() {
        return Result.ok(dashboardService.indexTrend());
    }

    /**
     * 抢修统计: 平均复电时长/完成率
     */
    @PostMapping("/repair-stat")
    public Result<Map<String, Object>> repairStat() {
        return Result.ok(dashboardService.repairStat());
    }

    /**
     * 治理进度
     */
    @PostMapping("/governance-progress")
    public Result<Map<String, Object>> governanceProgress() {
        return Result.ok(dashboardService.governanceProgress());
    }

    /**
     * 台区热力图数据
     */
    @PostMapping("/area-heatmap")
    public Result<List<Map<String, Object>>> areaHeatmap() {
        return Result.ok(dashboardService.areaHeatmap());
    }

    /**
     * 预警清单
     */
    @PostMapping("/warning-list")
    public Result<List<Map<String, Object>>> warningList() {
        return Result.ok(dashboardService.warningList());
    }

    /**
     * 班组排名
     */
    @PostMapping("/ranking")
    public Result<List<Map<String, Object>>> ranking() {
        return Result.ok(dashboardService.ranking());
    }
}
