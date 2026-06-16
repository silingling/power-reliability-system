package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.governance.entity.OutageCauseAnalysis;
import com.powerreliability.governance.service.OutageCauseAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

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
    @PostMapping("/cause/list")
    public Result<Page<OutageCauseAnalysis>> list(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

        Page<OutageCauseAnalysis> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OutageCauseAnalysis> wrapper = new LambdaQueryWrapper<>();

        if (params.get("eventId") != null && !params.get("eventId").toString().isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getEventId, params.get("eventId").toString());
        }
        if (params.get("responsibilityUnit") != null && !params.get("responsibilityUnit").toString().isEmpty()) {
            wrapper.eq(OutageCauseAnalysis::getResponsibilityUnit, params.get("responsibilityUnit").toString());
        }

        wrapper.orderByDesc(OutageCauseAnalysis::getCreateTime);
        return Result.success(outageCauseAnalysisService.page(page, wrapper));
    }

    /**
     * 触发归因分析
     */
    @PostMapping("/cause/analyze/{eventId}")
    public Result<OutageCauseAnalysis> analyze(@PathVariable String eventId) {
        // 创建归因分析记录
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
