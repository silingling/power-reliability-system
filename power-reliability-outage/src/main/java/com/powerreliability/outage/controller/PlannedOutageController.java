package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.PlannedOutage;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
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

    @Autowired
    private OutageEventMapper outageEventMapper;

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
    @Operation(summary = "提交计划停电申请（含「5次/年、60天/3次」合规校验）")
    public Result<Void> submit(@RequestBody PlannedOutage plannedOutage) {
        if (plannedOutage.getPlanStartTime() == null || plannedOutage.getPlanEndTime() == null) {
            return Result.fail("计划停电时间不能为空");
        }
        if (plannedOutage.getPlanEndTime().isBefore(plannedOutage.getPlanStartTime())) {
            return Result.fail("计划复电时间不能早于计划停电时间");
        }

        // ===== 「5次/年、60天/3次」新政合规校验 =====
        Long eventId = plannedOutage.getEventId();
        Long areaId = null;
        String areaName = "";

        if (eventId != null) {
            OutageEvent event = outageEventMapper.selectById(eventId);
            if (event != null) {
                areaId = event.getAreaId();
                areaName = event.getAreaName();
            }
        }

        if (areaId == null) {
            plannedOutage.setComplianceCheck(1);
            plannedOutage.setComplianceDetail("无关联停电事件，跳过频次校验");
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yearAgo = now.minusYears(1);
            LocalDateTime sixtyDaysAgo = now.minusDays(60);

            // 查询该台区近一年已闭环停电事件
            LambdaQueryWrapper<OutageEvent> yearWrapper = new LambdaQueryWrapper<>();
            yearWrapper.eq(OutageEvent::getAreaId, areaId)
                    .eq(OutageEvent::getIsClosed, 1)
                    .ge(OutageEvent::getOutageStartTime, yearAgo);
            long yearCount = outageEventMapper.selectCount(yearWrapper);

            // 查询该台区近60天已闭环停电事件
            LambdaQueryWrapper<OutageEvent> days60Wrapper = new LambdaQueryWrapper<>();
            days60Wrapper.eq(OutageEvent::getAreaId, areaId)
                    .eq(OutageEvent::getIsClosed, 1)
                    .ge(OutageEvent::getOutageStartTime, sixtyDaysAgo);
            long days60Count = outageEventMapper.selectCount(days60Wrapper);

            boolean yearCheckFailed = yearCount >= 5;
            boolean dayCheckFailed = days60Count >= 3;

            if (yearCheckFailed || dayCheckFailed) {
                StringBuilder detail = new StringBuilder("合规校验未通过: ");
                if (yearCheckFailed) {
                    detail.append("近一年已停电").append(yearCount).append("次（阈值5次）");
                }
                if (dayCheckFailed) {
                    if (yearCheckFailed) detail.append("; ");
                    detail.append("近60天已停电").append(days60Count).append("次（阈值3次）");
                }
                detail.append("。台区[").append(areaName).append("]");

                plannedOutage.setComplianceCheck(0);
                plannedOutage.setComplianceDetail(detail.toString());
                plannedOutage.setApprovalStatus(0);
                plannedOutage.setExecutionStatus(0);
                plannedOutageService.save(plannedOutage);
                return Result.fail(detail.toString());
            } else {
                plannedOutage.setComplianceCheck(1);
                plannedOutage.setComplianceDetail("合规校验通过: 近一年停电" + yearCount + "次（<5），近60天停电" + days60Count + "次（<3）");
            }
        }

        plannedOutage.setApprovalStatus(0);
        plannedOutage.setExecutionStatus(0);

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
