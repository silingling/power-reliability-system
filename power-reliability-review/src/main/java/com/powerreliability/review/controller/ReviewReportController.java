package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.review.entity.ReviewReport;
import com.powerreliability.review.service.ReviewReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "停电复盘报告管理")
@RestController
@RequestMapping("/api/review/report")
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @Operation(summary = "分页查询复盘报告列表")
    @GetMapping("/list")
    public Result<PageResult<ReviewReport>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer outageType,
            @RequestParam(required = false) Integer reportStatus,
            @RequestParam(required = false) String outageEventNo,
            @RequestParam(required = false) String keywords) {
        Page<ReviewReport> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ReviewReport> wrapper = new LambdaQueryWrapper<>();
        if (outageType != null) {
            wrapper.eq(ReviewReport::getOutageType, outageType);
        }
        if (reportStatus != null) {
            wrapper.eq(ReviewReport::getReportStatus, reportStatus);
        }
        if (outageEventNo != null && !outageEventNo.isEmpty()) {
            wrapper.like(ReviewReport::getOutageEventNo, outageEventNo);
        }
        if (keywords != null && !keywords.isEmpty()) {
            wrapper.like(ReviewReport::getReportTitle, keywords);
        }
        wrapper.orderByDesc(ReviewReport::getCreateTime);
        reviewReportService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(pageParam.getRecords(), pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize()));
    }

    @Operation(summary = "导出复盘报告Excel")
    @PostMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer outageType,
            @RequestParam(required = false) Integer reportStatus,
            @RequestParam(required = false) String outageEventNo,
            @RequestParam(required = false) String keywords) {
        LambdaQueryWrapper<ReviewReport> wrapper = new LambdaQueryWrapper<>();
        if (outageType != null) wrapper.eq(ReviewReport::getOutageType, outageType);
        if (reportStatus != null) wrapper.eq(ReviewReport::getReportStatus, reportStatus);
        if (outageEventNo != null && !outageEventNo.isEmpty()) wrapper.like(ReviewReport::getOutageEventNo, outageEventNo);
        if (keywords != null && !keywords.isEmpty()) wrapper.like(ReviewReport::getReportTitle, keywords);
        wrapper.orderByDesc(ReviewReport::getCreateTime);
        List<ReviewReport> list = reviewReportService.list(wrapper);
        ExcelExportUtil.export(response, list, "复盘报告导出");
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
    @GetMapping("/detail/{id}")
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
    public static class CreateRequest {
        private Long outageEventId;
    }
}
