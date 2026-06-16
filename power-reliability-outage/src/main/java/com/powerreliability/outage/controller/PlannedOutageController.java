package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.outage.entity.PlannedOutage;
import com.powerreliability.outage.service.PlannedOutageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/planned")
public class PlannedOutageController {

    @Autowired
    private PlannedOutageService plannedOutageService;

    @PostMapping("/list")
    public Result<PageResult<PlannedOutage>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<PlannedOutage> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PlannedOutage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PlannedOutage::getCreateTime);

        if (params.get("approvalStatus") != null && !params.get("approvalStatus").toString().isEmpty()) {
            wrapper.eq(PlannedOutage::getApprovalStatus, Integer.parseInt(params.get("approvalStatus").toString()));
        }
        if (params.get("executionStatus") != null && !params.get("executionStatus").toString().isEmpty()) {
            wrapper.eq(PlannedOutage::getExecutionStatus, Integer.parseInt(params.get("executionStatus").toString()));
        }

        Page<PlannedOutage> result = plannedOutageService.page(pageParam, wrapper);
        PageResult<PlannedOutage> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody PlannedOutage plannedOutage) {
        // Compliance check: verify plan time is reasonable, content is complete
        if (plannedOutage.getPlanStartTime() == null || plannedOutage.getPlanEndTime() == null) {
            return Result.fail("计划停电时间不能为空");
        }
        if (plannedOutage.getPlanEndTime().isBefore(plannedOutage.getPlanStartTime())) {
            return Result.fail("计划复电时间不能早于计划停电时间");
        }

        // Set initial audit status
        plannedOutage.setApprovalStatus(0); // 0-待审批
        plannedOutage.setExecutionStatus(0); // 0-未开始
        plannedOutage.setComplianceCheck(1); // 合规校验通过

        plannedOutageService.save(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        Integer approved = params.get("approved") != null ? Integer.parseInt(params.get("approved").toString()) : null;

        if (id == null) {
            return Result.fail("ID不能为空");
        }

        PlannedOutage plannedOutage = plannedOutageService.getById(id);
        if (plannedOutage == null) {
            return Result.fail("计划停电记录不存在");
        }

        if (approved != null && approved == 1) {
            plannedOutage.setApprovalStatus(2); // 已通过
        } else {
            plannedOutage.setApprovalStatus(3); // 已驳回
        }

        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/execute")
    public Result<Void> execute(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        if (id == null) {
            return Result.fail("ID不能为空");
        }

        PlannedOutage plannedOutage = plannedOutageService.getById(id);
        if (plannedOutage == null) {
            return Result.fail("计划停电记录不存在");
        }

        plannedOutage.setExecutionStatus(1); // 执行中
        plannedOutage.setActualStartTime(LocalDateTime.now());
        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/verify")
    public Result<Void> verify(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        if (id == null) {
            return Result.fail("ID不能为空");
        }

        PlannedOutage plannedOutage = plannedOutageService.getById(id);
        if (plannedOutage == null) {
            return Result.fail("计划停电记录不存在");
        }

        plannedOutage.setExecutionStatus(2); // 已完成
        plannedOutage.setActualEndTime(LocalDateTime.now());

        // Calculate overtime
        if (plannedOutage.getActualEndTime().isAfter(plannedOutage.getPlanEndTime())) {
            plannedOutage.setIsOvertime(1);
            long diffMinutes = java.time.Duration.between(plannedOutage.getPlanEndTime(), plannedOutage.getActualEndTime()).toMinutes();
            plannedOutage.setOvertimeMinutes((int) diffMinutes);
        } else {
            plannedOutage.setIsOvertime(0);
            plannedOutage.setOvertimeMinutes(0);
        }

        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }
}
