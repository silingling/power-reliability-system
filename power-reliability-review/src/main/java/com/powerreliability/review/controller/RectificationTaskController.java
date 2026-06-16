package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.review.entity.RectificationTask;
import com.powerreliability.review.service.RectificationTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "整改任务管理")
@RestController
@RequestMapping("/api/review/rectification")
@RequiredArgsConstructor
public class RectificationTaskController {

    private final RectificationTaskService rectificationTaskService;

    @Operation(summary = "分页查询整改任务列表")
    @GetMapping("/list")
    public Result<PageResult<RectificationTask>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer taskStatus,
            @RequestParam(required = false) Integer rectificationType,
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) String responsibleDept) {
        Page<RectificationTask> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<RectificationTask> wrapper = new LambdaQueryWrapper<>();
        if (taskStatus != null) {
            wrapper.eq(RectificationTask::getTaskStatus, taskStatus);
        }
        if (rectificationType != null) {
            wrapper.eq(RectificationTask::getRectificationType, rectificationType);
        }
        if (reportId != null) {
            wrapper.eq(RectificationTask::getReportId, reportId);
        }
        if (responsibleDept != null && !responsibleDept.isEmpty()) {
            wrapper.eq(RectificationTask::getResponsibleDept, responsibleDept);
        }
        wrapper.orderByDesc(RectificationTask::getCreateTime);
        rectificationTaskService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(pageParam.getRecords(), pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize()));
    }

    @Operation(summary = "导出整改任务Excel")
    @PostMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer taskStatus,
            @RequestParam(required = false) Integer rectificationType,
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) String responsibleDept) {
        LambdaQueryWrapper<RectificationTask> wrapper = new LambdaQueryWrapper<>();
        if (taskStatus != null) wrapper.eq(RectificationTask::getTaskStatus, taskStatus);
        if (rectificationType != null) wrapper.eq(RectificationTask::getRectificationType, rectificationType);
        if (reportId != null) wrapper.eq(RectificationTask::getReportId, reportId);
        if (responsibleDept != null && !responsibleDept.isEmpty()) wrapper.eq(RectificationTask::getResponsibleDept, responsibleDept);
        wrapper.orderByDesc(RectificationTask::getCreateTime);
        List<RectificationTask> list = rectificationTaskService.list(wrapper);
        ExcelExportUtil.export(response, list, "整改任务导出");
    }

    @Operation(summary = "根据复盘报告生成整改任务")
    @PostMapping("/create")
    public Result<List<RectificationTask>> create(@RequestBody CreateRequest request) {
        if (request.getReportId() == null) {
            return Result.fail("复盘报告ID不能为空");
        }
        List<RectificationTask> tasks = rectificationTaskService.createFromReport(request.getReportId());
        return Result.ok(tasks);
    }

    @Operation(summary = "完成整改任务")
    @PostMapping("/complete/{id}")
    public Result<String> complete(@PathVariable Long id, @RequestBody CompleteRequest request) {
        boolean ok = rectificationTaskService.complete(id, request.getCompleteDesc());
        if (!ok) {
            return Result.fail("完成失败，请检查任务状态");
        }
        return Result.ok("整改任务已完成");
    }

    @Operation(summary = "更新整改任务")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody RectificationTask task) {
        if (task.getId() == null) {
            return Result.fail("任务ID不能为空");
        }
        rectificationTaskService.updateById(task);
        return Result.ok();
    }

    @Operation(summary = "验收整改任务")
    @PostMapping("/accept/{id}")
    public Result<String> accept(@PathVariable Long id, @RequestBody AcceptRequest request) {
        boolean ok = rectificationTaskService.accept(id, request.getAcceptResult(), request.getAcceptOpinion());
        if (!ok) {
            return Result.fail("验收失败，请检查任务状态");
        }
        return Result.ok("验收完成");
    }

    @lombok.Data
    public static class CreateRequest {
        private Long reportId;
    }

    @lombok.Data
    public static class CompleteRequest {
        private String completeDesc;
    }

    @lombok.Data
    public static class AcceptRequest {
        private Integer acceptResult;
        private String acceptOpinion;
    }
}
