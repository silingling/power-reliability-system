package com.powerreliability.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.warning.entity.RiskWarningOrder;
import com.powerreliability.warning.service.RiskWarningOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "隐患预警工单管理")
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class RiskWarningOrderController {

    private final RiskWarningOrderService riskWarningOrderService;

    @Operation(summary = "分页查询预警工单列表")
    @GetMapping("/order/list")
    public Result<PageResult<RiskWarningOrder>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer disposeStatus,
            @RequestParam(required = false) Integer urgencyLevel,
            @RequestParam(required = false) Integer warningType,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String keywords) {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;
        Page<RiskWarningOrder> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RiskWarningOrder> wrapper = new LambdaQueryWrapper<>();
        if (disposeStatus != null) {
            wrapper.eq(RiskWarningOrder::getDisposeStatus, disposeStatus);
        }
        if (urgencyLevel != null) {
            wrapper.eq(RiskWarningOrder::getUrgencyLevel, urgencyLevel);
        }
        if (warningType != null) {
            wrapper.eq(RiskWarningOrder::getWarningType, warningType);
        }
        if (equipmentId != null) {
            wrapper.eq(RiskWarningOrder::getEquipmentId, equipmentId);
        }
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(RiskWarningOrder::getWarningTitle, keywords)
                    .or(w -> w.like(RiskWarningOrder::getWarningContent, keywords));
        }
        wrapper.orderByDesc(RiskWarningOrder::getCreateTime);
        riskWarningOrderService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(pageParam.getRecords(), pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize()));
    }

    @Operation(summary = "导出预警工单Excel")
    @PostMapping("/order/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer disposeStatus,
            @RequestParam(required = false) Integer urgencyLevel,
            @RequestParam(required = false) Integer warningType,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<RiskWarningOrder> wrapper = new LambdaQueryWrapper<>();
        if (disposeStatus != null) wrapper.eq(RiskWarningOrder::getDisposeStatus, disposeStatus);
        if (urgencyLevel != null) wrapper.eq(RiskWarningOrder::getUrgencyLevel, urgencyLevel);
        if (warningType != null) wrapper.eq(RiskWarningOrder::getWarningType, warningType);
        if (equipmentId != null) wrapper.eq(RiskWarningOrder::getEquipmentId, equipmentId);
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(RiskWarningOrder::getWarningTitle, keywords)
                    .or(w -> w.like(RiskWarningOrder::getWarningContent, keywords));
        }
        wrapper.orderByDesc(RiskWarningOrder::getCreateTime);
        List<RiskWarningOrder> list = riskWarningOrderService.list(wrapper);
        ExcelExportUtil.export(response, list, "预警工单导出");
    }

    @Operation(summary = "自动生成预警工单")
    @PostMapping("/order/dispatch")
    public Result<String> dispatch() {
        int count = riskWarningOrderService.dispatchWarning();
        return Result.ok("预警工单下发完成，共生成 " + count + " 条工单");
    }

    @Operation(summary = "处置预警工单")
    @PostMapping("/order/dispose/{id}")
    public Result<String> dispose(@PathVariable Long id, @RequestBody DisposeRequest request) {
        boolean ok = riskWarningOrderService.dispose(id, request.getDisposeDesc());
        if (!ok) {
            return Result.fail("处置失败，请检查工单状态");
        }
        return Result.ok("处置成功");
    }

    @Operation(summary = "更新预警工单")
    @PutMapping("/order/update")
    public Result<Void> update(@RequestBody RiskWarningOrder order) {
        if (order.getId() == null) {
            return Result.fail("工单ID不能为空");
        }
        riskWarningOrderService.updateById(order);
        return Result.ok();
    }

    @Operation(summary = "复核预警工单")
    @PostMapping("/order/review/{id}")
    public Result<String> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        boolean ok = riskWarningOrderService.review(id, request.getReviewResult());
        if (!ok) {
            return Result.fail("复核失败，请检查工单状态");
        }
        return Result.ok("复核完成");
    }

    @lombok.Data
    public static class DisposeRequest {
        private String disposeDesc;
    }

    @lombok.Data
    public static class ReviewRequest {
        private Integer reviewResult;
    }
}
