package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.governance.entity.GovernanceOrder;
import com.powerreliability.governance.exception.GovernanceException;
import com.powerreliability.governance.service.GovernanceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 治理工单控制器
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@Tag(name = "治理工单管理")
public class GovernanceOrderController {

    private final GovernanceOrderService governanceOrderService;

    /**
     * 分页查询工单
     */
    @GetMapping("/order/list")
    @Operation(summary = "分页查询治理工单")
    public Result<Page<GovernanceOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String responsibleUnit) {
        Page<GovernanceOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GovernanceOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(GovernanceOrder::getStatus, status);
        }
        if (eventId != null && !eventId.isEmpty()) {
            wrapper.eq(GovernanceOrder::getEventId, eventId);
        }
        if (responsibleUnit != null && !responsibleUnit.isEmpty()) {
            wrapper.eq(GovernanceOrder::getResponsibleUnit, responsibleUnit);
        }
        wrapper.orderByDesc(GovernanceOrder::getCreateTime);
        Page<GovernanceOrder> resultPage = governanceOrderService.page(page, wrapper);
        return Result.ok(PageResult.of(resultPage.getRecords(), resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize()));
    }

    /**
     * 创建工单
     */
    @PostMapping("/order/create")
    @Operation(summary = "创建治理工单")
    public Result<GovernanceOrder> create(@RequestBody GovernanceOrder order) {
        order.setId(null);
        order.setStatus(0);
        order.setReviewResult(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        governanceOrderService.save(order);
        return Result.ok(order);
    }

    /**
     * 分配工单
     */
    @PostMapping("/order/dispatch")
    @Operation(summary = "分配治理工单")
    public Result<Void> dispatch(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String unit = params.get("responsibleUnit").toString();
        String person = params.get("responsiblePerson").toString();
        String deadline = params.get("deadline").toString();
        try {
            governanceOrderService.dispatch(id, unit, person, deadline);
            return Result.ok();
        } catch (GovernanceException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 提交审核
     */
    @PostMapping("/order/submit/{id}")
    @Operation(summary = "提交审核")
    public Result<Void> submit(@PathVariable Long id) {
        try {
            governanceOrderService.submitForReview(id);
            return Result.ok();
        } catch (GovernanceException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核工单
     */
    @PostMapping("/order/accept/{id}")
    @Operation(summary = "审核治理工单")
    public Result<Void> accept(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer result = Integer.parseInt(params.get("result").toString());
        try {
            governanceOrderService.acceptReview(id, result);
            return Result.ok();
        } catch (GovernanceException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 导出治理工单Excel
     */
    @PostMapping("/order/export")
    @Operation(summary = "导出治理工单Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String eventId,
            @RequestParam(required = false) String responsibleUnit,
            @RequestParam(required = false) String responsiblePerson,
            @RequestParam(required = false) String orderNo) {
        LambdaQueryWrapper<GovernanceOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(GovernanceOrder::getStatus, status);
        }
        if (eventId != null && !eventId.isEmpty()) {
            wrapper.eq(GovernanceOrder::getEventId, eventId);
        }
        if (responsibleUnit != null && !responsibleUnit.isEmpty()) {
            wrapper.eq(GovernanceOrder::getResponsibleUnit, responsibleUnit);
        }
        if (responsiblePerson != null && !responsiblePerson.isEmpty()) {
            wrapper.eq(GovernanceOrder::getResponsiblePerson, responsiblePerson);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(GovernanceOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(GovernanceOrder::getCreateTime);
        List<GovernanceOrder> list = governanceOrderService.list(wrapper);
        ExcelExportUtil.export(response, list, "治理工单导出");
    }

    /**
     * 更新工单
     */
    @PutMapping("/order/update")
    @Operation(summary = "更新治理工单")
    public Result<Void> update(@RequestBody GovernanceOrder order) {
        if (order.getId() == null) {
            return Result.fail("工单ID不能为空");
        }
        governanceOrderService.updateById(order);
        return Result.ok();
    }
}
