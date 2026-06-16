package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.engine.OutageCauseEngine;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.governance.entity.OutageCauseAnalysis;
import com.powerreliability.governance.service.OutageCauseAnalysisService;
import com.powerreliability.ledger.entity.Equipment;
import com.powerreliability.ledger.mapper.EquipmentMapper;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停电归因分析控制器
 * 使用 OutageCauseEngine 进行 8 类标准诱因归因
 */
@Slf4j
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class OutageCauseAnalysisController {

    private final OutageCauseEngine outageCauseEngine;
    private final OutageCauseAnalysisService outageCauseAnalysisService;
    private final OutageEventMapper outageEventMapper;
    private final EquipmentMapper equipmentMapper;

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
     * 触发归因分析 — 使用 OutageCauseEngine 多因子算法
     *
     * @param eventId 停电事件编号
     * @return 归因分析结果
     */
    @PostMapping("/cause/analyze/{eventId}")
    @Operation(summary = "触发归因分析(多因子算法)")
    public Result<OutageCauseAnalysis> analyze(@PathVariable String eventId) {
        // 1. 查询关联的停电事件
        LambdaQueryWrapper<OutageEvent> eventWrapper = new LambdaQueryWrapper<>();
        eventWrapper.eq(OutageEvent::getEventNo, eventId);
        OutageEvent event = outageEventMapper.selectOne(eventWrapper);

        Integer equipmentAge = null;
        Integer faultCount = null;
        Double weatherFactor = null;

        if (event != null && event.getAreaId() != null) {
            // 2. 查找关联设备，获取设备老化年限和故障次数
            LambdaQueryWrapper<Equipment> eqWrapper = new LambdaQueryWrapper<>();
            eqWrapper.eq(Equipment::getAreaId, event.getAreaId());
            List<Equipment> equipments = equipmentMapper.selectList(eqWrapper);

            if (!equipments.isEmpty()) {
                // 计算平均设备老化年限
                double avgAge = equipments.stream()
                        .filter(e -> e.getCommissioningDate() != null)
                        .mapToLong(e -> ChronoUnit.YEARS.between(e.getCommissioningDate(), LocalDate.now()))
                        .average().orElse(0);
                equipmentAge = (int) avgAge;

                // 累加故障次数
                faultCount = equipments.stream()
                        .filter(e -> e.getFaultCount() != null)
                        .mapToInt(Equipment::getFaultCount)
                        .sum();
            }
        }

        // 3. 调用 OutageCauseEngine 多因子归因
        OutageCauseEngine.CauseResult causeResult = outageCauseEngine.analyze(
                equipmentAge,
                faultCount,
                null,             // 负荷率（暂缺）
                weatherFactor     // 天气影响因子（暂缺）
        );

        // 4. 构建归因分析记录
        OutageCauseAnalysis analysis = new OutageCauseAnalysis();
        analysis.setEventId(eventId);
        analysis.setEventName(event != null ? event.getFaultReason() : "停电事件 " + eventId);

        // Top3 诱因 + 置信度合并写入 analysisResult
        String top3Str = causeResult.getTop3Causes().stream()
                .map(f -> f.getCauseName() + "(" + f.getScore() + "%)")
                .collect(Collectors.joining(" → "));
        analysis.setAnalysisResult("8类归因完成: " + top3Str
                + " | 主要诱因置信度: " + causeResult.getConfidence() + "%");

        analysis.setDirectCause(event != null ? event.getFaultReason() : "线路故障");
        analysis.setCauseType(causeResult.getPrimaryCauseType());
        analysis.setRootCause(causeResult.getPrimaryCauseName());

        // 5. 根据主要归因类型确定责任单位
        String responsibilityUnit;
        switch (causeResult.getPrimaryCauseType()) {
            case 1: case 2: case 3:
                responsibilityUnit = "运维检修部"; break;
            case 4: case 5:
                responsibilityUnit = "调度控制中心"; break;
            case 6:
                responsibilityUnit = "安监部"; break;
            case 7:
                responsibilityUnit = "工程建设部"; break;
            default:
                responsibilityUnit = "属地供电所";
        }
        analysis.setResponsibilityUnit(responsibilityUnit);
        analysis.setAnalysisTime(LocalDateTime.now());
        analysis.setRemarks("引擎置信度: " + causeResult.getConfidence() + "%");

        outageCauseAnalysisService.save(analysis);

        log.info("[OutageCauseAnalysis] 归因完成: eventId={}, primary={}, confidence={}%",
                eventId, causeResult.getPrimaryCauseName(), causeResult.getConfidence());

        return Result.success(analysis);
    }
}
