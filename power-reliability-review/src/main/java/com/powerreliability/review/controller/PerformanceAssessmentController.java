package com.powerreliability.review.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.review.entity.PerformanceAssessment;
import com.powerreliability.review.service.PerformanceAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "绩效考核管理")
@RestController
@RequestMapping("/api/review/assessment")
@RequiredArgsConstructor
public class PerformanceAssessmentController {

    private final PerformanceAssessmentService performanceAssessmentService;

    @Operation(summary = "分页查询绩效考核列表")
    @PostMapping("/list")
    public Result<PageResult<PerformanceAssessment>> list(@RequestBody(required = false) AssessmentQuery query) {
        if (query == null) {
            query = new AssessmentQuery();
        }
        Page<PerformanceAssessment> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<PerformanceAssessment> wrapper = new LambdaQueryWrapper<>();
        if (query.getAssessmentPeriod() != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentPeriod, query.getAssessmentPeriod());
        }
        if (query.getAssessmentYear() != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentYear, query.getAssessmentYear());
        }
        if (query.getAssessmentStatus() != null) {
            wrapper.eq(PerformanceAssessment::getAssessmentStatus, query.getAssessmentStatus());
        }
        if (query.getAssessmentGrade() != null && !query.getAssessmentGrade().isEmpty()) {
            wrapper.eq(PerformanceAssessment::getAssessmentGrade, query.getAssessmentGrade());
        }
        if (query.getTargetName() != null && !query.getTargetName().isEmpty()) {
            wrapper.like(PerformanceAssessment::getTargetName, query.getTargetName());
        }
        wrapper.orderByDesc(PerformanceAssessment::getCreateTime);
        performanceAssessmentService.page(page, wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @Operation(summary = "自动计算绩效评分")
    @PostMapping("/calculate")
    public Result<String> calculate(@RequestBody(required = false) CalculateRequest request) {
        Long id = (request != null) ? request.getId() : null;
        int count = performanceAssessmentService.calculate(id);
        return Result.ok("绩效评分计算完成，共计算 " + count + " 条记录");
    }

    @Operation(summary = "查询绩效考核详情")
    @PostMapping("/detail/{id}")
    public Result<PerformanceAssessment> detail(@PathVariable Long id) {
        PerformanceAssessment assessment = performanceAssessmentService.getById(id);
        if (assessment == null) {
            return Result.fail("考核记录不存在");
        }
        return Result.ok(assessment);
    }

    @lombok.Data
    public static class AssessmentQuery {
        private Integer page = 1;
        private Integer pageSize = 10;
        private Integer assessmentPeriod;
        private Integer assessmentYear;
        private Integer assessmentStatus;
        private String assessmentGrade;
        private String targetName;
    }

    @lombok.Data
    public static class CalculateRequest {
        private Long id;
    }
}
