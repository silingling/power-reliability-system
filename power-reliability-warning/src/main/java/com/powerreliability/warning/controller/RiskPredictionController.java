package com.powerreliability.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.warning.entity.RiskPrediction;
import com.powerreliability.warning.service.RiskPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "隐患预判管理")
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class RiskPredictionController {

    private final RiskPredictionService riskPredictionService;

    @Operation(summary = "分页查询隐患预判列表")
    @GetMapping("/prediction/list")
    public Result<PageResult<RiskPrediction>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer predictionType,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String keywords) {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;
        Page<RiskPrediction> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RiskPrediction> wrapper = new LambdaQueryWrapper<>();
        if (riskLevel != null) {
            wrapper.eq(RiskPrediction::getRiskLevel, riskLevel);
        }
        if (status != null) {
            wrapper.eq(RiskPrediction::getStatus, status);
        }
        if (predictionType != null) {
            wrapper.eq(RiskPrediction::getPredictionType, predictionType);
        }
        if (equipmentId != null) {
            wrapper.eq(RiskPrediction::getEquipmentId, equipmentId);
        }
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(RiskPrediction::getPredictionDesc, keywords);
        }
        wrapper.orderByDesc(RiskPrediction::getCreateTime);
        riskPredictionService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(pageParam.getRecords(), pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize()));
    }

    @Operation(summary = "导出隐患预判Excel")
    @PostMapping("/prediction/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer riskLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer predictionType,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<RiskPrediction> wrapper = new LambdaQueryWrapper<>();
        if (riskLevel != null) wrapper.eq(RiskPrediction::getRiskLevel, riskLevel);
        if (status != null) wrapper.eq(RiskPrediction::getStatus, status);
        if (predictionType != null) wrapper.eq(RiskPrediction::getPredictionType, predictionType);
        if (equipmentId != null) wrapper.eq(RiskPrediction::getEquipmentId, equipmentId);
        if (keywords != null && !keywords.isEmpty()) wrapper.like(RiskPrediction::getPredictionDesc, keywords);
        wrapper.orderByDesc(RiskPrediction::getCreateTime);
        List<RiskPrediction> list = riskPredictionService.list(wrapper);
        ExcelExportUtil.export(response, list, "隐患预判导出");
    }

    @Operation(summary = "自动执行多因素风险评分预判")
    @PostMapping("/prediction/auto-predict")
    public Result<String> autoPredict() {
        int count = riskPredictionService.autoPredict();
        return Result.ok("自动预判完成，共生成 " + count + " 条隐患预判记录");
    }

    @Operation(summary = "查询隐患预判详情")
    @GetMapping("/prediction/detail/{id}")
    public Result<RiskPrediction> detail(@PathVariable Long id) {
        RiskPrediction prediction = riskPredictionService.getById(id);
        if (prediction == null) {
            return Result.fail("预判记录不存在");
        }
        return Result.ok(prediction);
    }
}
