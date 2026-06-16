package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.review.entity.RectificationTask;
import com.powerreliability.review.service.RectificationTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @PostMapping("/list")
    public Result<PageResult<RectificationTask>> list(@RequestBody(required = false) TaskQuery query) {
        if (query == null) {
            query = new TaskQuery();
        }
        Page<RectificationTask> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<RectificationTask> wrapper = new LambdaQueryWrapper<>();
        if (query.getTaskStatus() != null) {
            wrapper.eq(RectificationTask::getTaskStatus, query.getTaskStatus());
        }
        if (query.getRectificationType() != null) {
            wrapper.eq(RectificationTask::getRectificationType, query.getRectificationType());
        }
        if (query.getReportId() != null) {
            wrapper.eq(RectificationTask::getReportId, query.getReportId());
        }
        if (query.getResponsibleDept() != null && !query.getResponsibleDept().isEmpty()) {
            wrapper.eq(RectificationTask::getResponsibleDept, query.getResponsibleDept());
        }
        wrapper.orderByDesc(RectificationTask::getCreateTime);
        rectificationTaskService.page(page, wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
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
    public static class TaskQuery {
        private Integer page = 1;
        private Integer pageSize = 10;
        private Integer taskStatus;
        private Integer rectificationType;
        private Long reportId;
        private String responsibleDept;
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
