package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.review.entity.ReviewReport;
import com.powerreliability.review.service.ReviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "停电复盘报告管理")
@RestController
@RequestMapping("/api/review/report")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @Operation(summary = "分页查询复盘报告列表")
    @PostMapping("/list")
    public Result<PageResult<ReviewReport>> list(@RequestBody(required = false) ReportQuery query) {
        if (query == null) {
            query = new ReportQuery();
        }
        Page<ReviewReport> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ReviewReport> wrapper = new LambdaQueryWrapper<>();
        if (query.getOutageType() != null) {
            wrapper.eq(ReviewReport::getOutageType, query.getOutageType());
        }
        if (query.getReportStatus() != null) {
            wrapper.eq(ReviewReport::getReportStatus, query.getReportStatus());
        }
        if (query.getOutageEventNo() != null && !query.getOutageEventNo().isEmpty()) {
            wrapper.like(ReviewReport::getOutageEventNo, query.getOutageEventNo());
        }
        if (query.getKeywords() != null && !query.getKeywords().isEmpty()) {
            wrapper.like(ReviewReport::getReportTitle, query.getKeywords());
        }
        wrapper.orderByDesc(ReviewReport::getCreateTime);
        reviewReportService.page(page, wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "根据停电事件生成复盘报告")
    @PostMapping("/create")
    public Result<ReviewReport> create(@RequestBody CreateRequest request) {
        if (request.getOutageEventId() == null) {
            return Result.fail("停电事件ID不能为空");
        }
        ReviewReport report = reviewReportService.createFromOutage(request.getOutageEventId());
        return Result.ok(report);
    }

    @Operation(summary = "查询复盘报告详情")
    @PostMapping("/detail/{id}")
    public Result<ReviewReport> detail(@PathVariable Long id) {
        ReviewReport report = reviewReportService.getById(id);
        if (report == null) {
            return Result.fail("复盘报告不存在");
        }
        return Result.ok(report);
    }

    @Operation(summary = "发布复盘报告")
    @PostMapping("/publish/{id}")
    public Result<String> publish(@PathVariable Long id) {
        boolean ok = reviewReportService.publish(id);
        if (!ok) {
            return Result.fail("发布失败，请检查报告状态是否为草稿");
        }
        return Result.ok("发布成功");
    }

    @lombok.Data
    public static class ReportQuery {
        private Integer page = 1;
        private Integer pageSize = 10;
        private Integer outageType;
        private Integer reportStatus;
        private String outageEventNo;
        private String keywords;
    }

    @lombok.Data
    public static class CreateRequest {
        private Long outageEventId;
    }
}
