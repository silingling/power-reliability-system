package com.powerreliability.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.warning.entity.RiskPrediction;
import com.powerreliability.warning.service.RiskPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "隐患预判管理")
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class RiskPredictionController {

    private final RiskPredictionService riskPredictionService;

    @Operation(summary = "分页查询隐患预判列表")
    @PostMapping("/prediction/list")
    public Result<PageResult<RiskPrediction>> list(@RequestBody(required = false) RiskPredictionQuery query) {
        if (query == null) {
            query = new RiskPredictionQuery();
        }
        Page<RiskPrediction> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<RiskPrediction> wrapper = new LambdaQueryWrapper<>();
        if (query.getRiskLevel() != null) {
            wrapper.eq(RiskPrediction::getRiskLevel, query.getRiskLevel());
        }
        if (query.getStatus() != null) {
            wrapper.eq(RiskPrediction::getStatus, query.getStatus());
        }
        if (query.getPredictionType() != null) {
            wrapper.eq(RiskPrediction::getPredictionType, query.getPredictionType());
        }
        if (query.getEquipmentId() != null) {
            wrapper.eq(RiskPrediction::getEquipmentId, query.getEquipmentId());
        }
        if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            wrapper.like(RiskPrediction::getPredictionDesc, query.getKeywords());
        }
        wrapper.orderByDesc(RiskPrediction::getCreateTime);
        riskPredictionService.page(page, wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "自动执行多因素风险评分预判")
    @PostMapping("/prediction/auto-predict")
    public Result<String> autoPredict() {
        int count = riskPredictionService.autoPredict();
        return Result.ok("自动预判完成，共生成 " + count + " 条隐患预判记录");
    }

    @Operation(summary = "查询隐患预判详情")
    @PostMapping("/prediction/detail/{id}")
    public Result<RiskPrediction> detail(@PathVariable Long id) {
        RiskPrediction prediction = riskPredictionService.getById(id);
        if (prediction == null) {
            return Result.fail("预判记录不存在");
        }
        return Result.ok(prediction);
    }

    /** 分页查询参数 */
    @lombok.Data
    public static class RiskPredictionQuery {
        private Integer page = 1;
        private Integer pageSize = 10;
        private Integer riskLevel;
        private Integer status;
        private Integer predictionType;
        private Long equipmentId;
        private String keywords;
    }
}
