package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.governance.entity.OutageCauseAnalysis;
import com.powerreliability.governance.service.OutageCauseAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 停电归因分析控制器
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class OutageCauseAnalysisController {

    private final OutageCauseAnalysisService outageCauseAnalysisService;

    /**
     * 分页查询归因分析
     */
    @GetMapping("/cause/list")
    @Operation(summary = "分页查询归因分析")
    public Result<Page<OutageCauseAnalysis>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String responsibilityUnit) {
        Page<OutageCauseAnalysis> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OutageCauseAnalysis> wrapper = new LambdaQueryWrapper<>();
        if (eventId != null && !eventId.isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getEventId, eventId);
        }
        if (responsibilityUnit != null && !responsibilityUnit.isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getResponsibilityUnit, responsibilityUnit);
        }
        wrapper.orderByDesc(OutageCauseAnalysis::getCreateTime);
        return Result.success(outageCauseAnalysisService.page(page, wrapper));
    }

    /**
     * 导出归因分析Excel
     */
    @PostMapping("/cause/export")
    @Operation(summary = "导出归因分析Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String responsibilityUnit) {
        LambdaQueryWrapper<OutageCauseAnalysis> wrapper = new LambdaQueryWrapper<>();
        if (eventId != null && !eventId.isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getEventId, eventId);
        }
        if (responsibilityUnit != null && !responsibilityUnit.isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getResponsibilityUnit, responsibilityUnit);
        }
        wrapper.orderByDesc(OutageCauseAnalysis::getCreateTime);
        List<OutageCauseAnalysis> list = outageCauseAnalysisService.list(wrapper);
        ExcelExportUtil.export(response, list, "停电归因分析导出");
    }

    /**
     * 触发归因分析
     */
    @PostMapping("/cause/analyze/{eventId}")
    @Operation(summary = "触发归因分析")
    public Result<OutageCauseAnalysis> analyze(@PathVariable String eventId) {
        OutageCauseAnalysis analysis = new OutageCauseAnalysis();
        analysis.setEventId(eventId);
        analysis.setEventName("停电事件 " + eventId);
        analysis.setAnalysisResult("分析完成");
        analysis.setDirectCause("线路故障");
        analysis.setRootCause("设备老化");
        analysis.setResponsibilityUnit("运维检修部");
        analysis.setAnalysisTime(LocalDateTime.now());
        outageCauseAnalysisService.save(analysis);
        return Result.success(analysis);
    }
}
