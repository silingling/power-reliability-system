package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.RepairOrder;
import com.powerreliability.outage.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/repair")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/list")
    @Operation(summary = "分页查询抢修单列表")
    public Result<PageResult<RepairOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Long eventId) {
        Page<RepairOrder> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();

        if (orderStatus != null) {
            wrapper.eq(RepairOrder::getOrderStatus, orderStatus);
        }
        if (eventId != null) {
            wrapper.eq(RepairOrder::getEventId, eventId);
        }

        wrapper.orderByDesc(RepairOrder::getCreateTime);
        Page<RepairOrder> result = repairOrderService.page(pageParam, wrapper);
        PageResult<RepairOrder> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出抢修单Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Long eventId) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderStatus != null) {
            wrapper.eq(RepairOrder::getOrderStatus, orderStatus);
        }
        if (eventId != null) {
            wrapper.eq(RepairOrder::getEventId, eventId);
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        List<RepairOrder> list = repairOrderService.list(wrapper);
        ExcelExportUtil.export(response, list, "抢修单导出");
    }

    @PostMapping("/dispatch")
    @Operation(summary = "派发抢修单")
    public Result<Void> dispatch(@RequestBody RepairOrder repairOrder) {
        repairOrder.setOrderStatus(1);
        repairOrder.setDispatchTime(LocalDateTime.now());
        repairOrderService.save(repairOrder);
        return Result.ok();
    }

    @PostMapping("/start/{id}")
    @Operation(summary = "开始抢修")
    public Result<Void> start(@PathVariable Long id) {
        repairOrderService.startRepair(id);
        return Result.ok();
    }

    @PostMapping("/complete/{id}")
    @Operation(summary = "完成抢修")
    public Result<Void> complete(@PathVariable Long id) {
        repairOrderService.completeRepair(id);
        return Result.ok();
    }

    @PostMapping("/verify/{id}")
    @Operation(summary = "核验抢修结果")
    public Result<Void> verify(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer result = params.get("result") != null ? Integer.parseInt(params.get("result").toString()) : null;
        if (result == null) {
            return Result.fail("核验结果不能为空");
        }
        repairOrderService.verifyRepair(id, result);
        return Result.ok();
    }

    @PutMapping("/update")
    @Operation(summary = "更新抢修单")
    public Result<Void> update(@RequestBody RepairOrder repairOrder) {
        if (repairOrder.getId() == null) {
            return Result.fail("ID不能为空");
        }
        repairOrderService.updateById(repairOrder);
        return Result.ok();
    }
}
