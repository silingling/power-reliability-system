package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.outage.entity.RepairOrder;
import com.powerreliability.outage.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/repair")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping("/list")
    public Result<PageResult<RepairOrder>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<RepairOrder> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();

        if (params.get("orderStatus") != null && !params.get("orderStatus").toString().isEmpty()) {
            wrapper.eq(RepairOrder::getOrderStatus, Integer.parseInt(params.get("orderStatus").toString()));
        }
        if (params.get("eventId") != null && !params.get("eventId").toString().isEmpty()) {
            wrapper.eq(RepairOrder::getEventId, Long.parseLong(params.get("eventId").toString()));
        }

        wrapper.orderByDesc(RepairOrder::getCreateTime);
        Page<RepairOrder> result = repairOrderService.page(pageParam, wrapper);
        PageResult<RepairOrder> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/dispatch")
    public Result<Void> dispatch(@RequestBody RepairOrder repairOrder) {
        repairOrder.setOrderStatus(1); // 已派单
        repairOrder.setDispatchTime(LocalDateTime.now());
        repairOrderService.save(repairOrder);
        return Result.ok();
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        repairOrderService.startRepair(id);
        return Result.ok();
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        repairOrderService.completeRepair(id);
        return Result.ok();
    }

    @PostMapping("/verify/{id}")
    public Result<Void> verify(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer result = params.get("result") != null ? Integer.parseInt(params.get("result").toString()) : null;
        if (result == null) {
            return Result.fail("核验结果不能为空");
        }
        repairOrderService.verifyRepair(id, result);
        return Result.ok();
    }
}
