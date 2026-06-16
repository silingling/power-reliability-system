package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.review.entity.PerformanceAssessment;
import com.powerreliability.review.service.PerformanceAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "绩效考核管理")
@RestController
@RequestMapping("/api/review/assessment")
@RequiredArgsConstructor
public class PerformanceAssessmentController {

    private final PerformanceAssessmentService performanceAssessmentService;

    @Operation(summary = "分页查询绩效考核列表")
    @GetMapping("/list")
    public Result<PageResult<PerformanceAssessment>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer assessmentPeriod,
            @RequestParam(required = false) Integer assessmentYear,
            @RequestParam(required = false) Integer assessmentStatus,
            @RequestParam(required = false) String assessmentGrade,
            @RequestParam(required = false) String targetName) {
        Page<PerformanceAssessment> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<PerformanceAssessment> wrapper = new LambdaQueryWrapper<>();
        if (assessmentPeriod != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentPeriod, assessmentPeriod);
        }
        if (assessmentYear != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentYear, assessmentYear);
        }
        if (assessmentStatus != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentStatus, assessmentStatus);
        }
        if (assessmentGrade != null && !assessmentGrade.isEmpty()) {
            wrapper.eq(PerformanceAssessment::getAssessmentGrade, assessmentGrade);
        }
        if (targetName != null && !targetName.isEmpty()) {
            wrapper.like(PerformanceAssessment::getTargetName, targetName);
        }
        wrapper.orderByDesc(PerformanceAssessment::getCreateTime);
        performanceAssessmentService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(pageParam.getRecords(), pageParam.getTotal(), pageParam.getCurrent(), pageParam.getSize()));
    }

    @Operation(summary = "导出绩效考核Excel")
    @PostMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer assessmentPeriod,
            @RequestParam(required = false) Integer assessmentYear,
            @RequestParam(required = false) Integer assessmentStatus,
            @RequestParam(required = false) String assessmentGrade,
            @RequestParam(required = false) String targetName) {
        LambdaQueryWrapper<PerformanceAssessment> wrapper = new LambdaQueryWrapper<>();
        if (assessmentPeriod != null) wrapper.eq(PerformanceAssessment::getAssessmentPeriod, assessmentPeriod);
        if (assessmentYear != null) wrapper.eq(PerformanceAssessment::getAssessmentYear, assessmentYear);
        if (assessmentStatus != null) wrapper.eq(PerformanceAssessment::getAssessmentStatus, assessmentStatus);
        if (assessmentGrade != null && !assessmentGrade.isEmpty()) wrapper.eq(PerformanceAssessment::getAssessmentGrade, assessmentGrade);
        if (targetName != null && !targetName.isEmpty()) wrapper.like(PerformanceAssessment::getTargetName, targetName);
        wrapper.orderByDesc(PerformanceAssessment::getCreateTime);
        List<PerformanceAssessment> list = performanceAssessmentService.list(wrapper);
        ExcelExportUtil.export(response, list, "绩效考核导出");
    }

    @Operation(summary = "自动计算绩效评分")
    @PostMapping("/calculate")
    public Result<String> calculate(@RequestBody(required = false) CalculateRequest request) {
        Long id = (request != null) ? request.getId() : null;
        int count = performanceAssessmentService.calculate(id);
        return Result.ok("绩效评分计算完成，共计算 " + count + " 条记录");
    }

    @Operation(summary = "查询绩效考核详情")
    @GetMapping("/detail/{id}")
    public Result<PerformanceAssessment> detail(@PathVariable Long id) {
        PerformanceAssessment assessment = performanceAssessmentService.getById(id);
        if (assessment == null) {
            return Result.fail("考核记录不存在");
        }
        return Result.ok(assessment);
    }

    @lombok.Data
    public static class CalculateRequest {
        private Long id;
    }
}
