package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.PlannedOutage;
import com.powerreliability.outage.service.PlannedOutageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/planned")
public class PlannedOutageController {

    @Autowired
    private PlannedOutageService plannedOutageService;

    @GetMapping("/list")
    @Operation(summary = "分页查询计划停电列表")
    public Result<PageResult<PlannedOutage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer approvalStatus,
            @RequestParam(required = false) Integer executionStatus) {
        Page<PlannedOutage> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PlannedOutage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PlannedOutage::getCreateTime);

        if (approvalStatus != null) {
            wrapper.eq(PlannedOutage::getApprovalStatus, approvalStatus);
        }
        if (executionStatus != null) {
            wrapper.eq(PlannedOutage::getExecutionStatus, executionStatus);
        }

        Page<PlannedOutage> result = plannedOutageService.page(pageParam, wrapper);
        PageResult<PlannedOutage> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出计划停电Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer approvalStatus,
            @RequestParam(required = false) Integer executionStatus) {
        LambdaQueryWrapper<PlannedOutage> wrapper = new LambdaQueryWrapper<>();
        if (approvalStatus != null) {
            wrapper.eq(PlannedOutage::getApprovalStatus, approvalStatus);
        }
        if (executionStatus != null) {
            wrapper.eq(PlannedOutage::getExecutionStatus, executionStatus);
        }
        wrapper.orderByDesc(PlannedOutage::getCreateTime);
        List<PlannedOutage> list = plannedOutageService.list(wrapper);
        ExcelExportUtil.export(response, list, "计划停电导出");
    }

    @PostMapping("/submit")
    @Operation(summary = "提交计划停电申请")
    public Result<Void> submit(@RequestBody PlannedOutage plannedOutage) {
        if (plannedOutage.getPlanStartTime() == null || plannedOutage.getPlanEndTime() == null) {
            return Result.fail("计划停电时间不能为空");
        }
        if (plannedOutage.getPlanEndTime().isBefore(plannedOutage.getPlanStartTime())) {
            return Result.fail("计划复电时间不能早于计划停电时间");
        }

        plannedOutage.setApprovalStatus(0);
        plannedOutage.setExecutionStatus(0);
        plannedOutage.setComplianceCheck(1);

        plannedOutageService.save(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/approve")
    @Operation(summary = "审批计划停电")
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
            plannedOutage.setApprovalStatus(2);
        } else {
            plannedOutage.setApprovalStatus(3);
        }

        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/execute")
    @Operation(summary = "执行计划停电")
    public Result<Void> execute(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        if (id == null) {
            return Result.fail("ID不能为空");
        }

        PlannedOutage plannedOutage = plannedOutageService.getById(id);
        if (plannedOutage == null) {
            return Result.fail("计划停电记录不存在");
        }

        plannedOutage.setExecutionStatus(1);
        plannedOutage.setActualStartTime(LocalDateTime.now());
        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }

    @PutMapping("/update")
    @Operation(summary = "更新计划停电")
    public Result<Void> update(@RequestBody PlannedOutage plannedOutage) {
        if (plannedOutage.getId() == null) {
            return Result.fail("ID不能为空");
        }
        plannedOutageService.updateById(plannedOutage);
        return Result.ok();
    }

    @PostMapping("/verify")
    @Operation(summary = "核验计划停电")
    public Result<Void> verify(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.parseLong(params.get("id").toString()) : null;
        if (id == null) {
            return Result.fail("ID不能为空");
        }

        PlannedOutage plannedOutage = plannedOutageService.getById(id);
        if (plannedOutage == null) {
            return Result.fail("计划停电记录不存在");
        }

        plannedOutage.setExecutionStatus(2);
        plannedOutage.setActualEndTime(LocalDateTime.now());

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
