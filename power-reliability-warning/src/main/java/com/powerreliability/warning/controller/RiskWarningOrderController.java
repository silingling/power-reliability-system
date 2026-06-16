package com.powerreliability.warning.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.warning.entity.RiskWarningOrder;
import com.powerreliability.warning.service.RiskWarningOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "隐患预警工单管理")
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class RiskWarningOrderController {

    private final RiskWarningOrderService riskWarningOrderService;

    @Operation(summary = "分页查询预警工单列表")
    @PostMapping("/order/list")
    public Result<PageResult<RiskWarningOrder>> list(@RequestBody(required = false) WarningOrderQuery query) {
        if (query == null) {
            query = new WarningOrderQuery();
        }
        Page<RiskWarningOrder> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<RiskWarningOrder> wrapper = new LambdaQueryWrapper<>();
        if (query.getDisposeStatus() != null) {
            wrapper.eq(RiskWarningOrder::getDisposeStatus, query.getDisposeStatus());
        }
        if (query.getUrgencyLevel() != null) {
            wrapper.eq(RiskWarningOrder::getUrgencyLevel, query.getUrgencyLevel());
        }
        if (query.getWarningType() != null) {
            wrapper.eq(RiskWarningOrder::getWarningType, query.getWarningType());
        }
        if (query.getEquipmentId() != null) {
            wrapper.eq(RiskWarningOrder::getEquipmentId, query.getEquipmentId());
        }
        if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            wrapper.like(RiskWarningOrder::getWarningTitle, query.getKeywords())
                    .or(w -> w.like(RiskWarningOrder::getWarningContent, query.getKeywords()));
        }
        wrapper.orderByDesc(RiskWarningOrder::getCreateTime);
        riskWarningOrderService.page(page, wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "自动生成预警工单（从预判记录下发）")
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

    @Operation(summary = "复核预警工单")
    @PostMapping("/order/review/{id}")
    public Result<String> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        boolean ok = riskWarningOrderService.review(id, request.getReviewResult());
        if (!ok) {
            return Result.fail("复核失败，请检查工单状态");
        }
        return Result.ok("复核完成");
    }

    /** 分页查询参数 */
    @lombok.Data
    public static class WarningOrderQuery {
        private Integer page = 1;
        private Integer pageSize = 10;
        private Integer disposeStatus;
        private Integer urgencyLevel;
        private Integer warningType;
        private Long equipmentId;
        private String keywords;
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
