package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.governance.entity.GovernanceOrder;
import com.powerreliability.governance.exception.GovernanceException;
import com.powerreliability.governance.service.GovernanceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 治理工单控制器
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class GovernanceOrderController {

    private final GovernanceOrderService governanceOrderService;

    /**
     * 分页查询工单
     */
    @PostMapping("/order/list")
    public Result<Page<GovernanceOrder>> list(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

        Page<GovernanceOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GovernanceOrder> wrapper = new LambdaQueryWrapper<>();

        if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
            wrapper.eq(GovernanceOrder::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        if (params.get("eventId") != null && !params.get("eventId").toString().isEmpty()) {
            wrapper.eq(GovernanceOrder::getEventId, params.get("eventId").toString());
        }
        if (params.get("responsibleUnit") != null && !params.get("responsibleUnit").toString().isEmpty()) {
            wrapper.eq(GovernanceOrder::getResponsibleUnit, params.get("responsibleUnit").toString());
        }

        wrapper.orderByDesc(GovernanceOrder::getCreateTime);
        return Result.success(governanceOrderService.page(page, wrapper));
    }

    /**
     * 创建工单
     */
    @PostMapping("/order/create")
    public Result<GovernanceOrder> create(@RequestBody GovernanceOrder order) {
        order.setId(null);
        order.setStatus(0);
        order.setReviewResult(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        governanceOrderService.save(order);
        return Result.success(order);
    }

    /**
     * 分配工单
     */
    @PostMapping("/order/dispatch")
    public Result<Void> dispatch(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String unit = params.get("responsibleUnit").toString();
        String person = params.get("responsiblePerson").toString();
        String deadline = params.get("deadline").toString();
        try {
            governanceOrderService.dispatch(id, unit, person, deadline);
            return Result.success();
        } catch (GovernanceException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交审核
     */
    @PostMapping("/order/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        try {
            governanceOrderService.submitForReview(id);
            return Result.success();
        } catch (GovernanceException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核工单
     */
    @PostMapping("/order/accept/{id}")
    public Result<Void> accept(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer result = Integer.parseInt(params.get("result").toString());
        try {
            governanceOrderService.acceptReview(id, result);
            return Result.success();
        } catch (GovernanceException e) {
            return Result.error(e.getMessage());
        }
    }
}
