package com.powerreliability.index.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.service.ReliabilityIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 可靠性指标统计控制器
 */
@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
@Tag(name = "可靠性指标管理")
public class ReliabilityIndexController {

    private final ReliabilityIndexService reliabilityIndexService;

    /**
     * 分页查询指标
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询可靠性指标")
    public Result<Page<ReliabilityIndex>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long statType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Integer period) {
        Page<ReliabilityIndex> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        if (statType != null) {
            wrapper.eq(ReliabilityIndex::getStatType, statType);
        }
        if (targetId != null) {
            wrapper.eq(ReliabilityIndex::getTargetId, targetId);
        }
        if (period != null) {
            wrapper.eq(ReliabilityIndex::getPeriod, period);
        }
        wrapper.orderByDesc(ReliabilityIndex::getStatDate);
        return Result.success(reliabilityIndexService.page(page, wrapper));
    }

    /**
     * 触发指标计算
     */
    @PostMapping("/calculate")
    @Operation(summary = "触发指标计算")
    public Result<Long> calculate(@RequestBody Map<String, Object> params) {
        Long statType = Long.parseLong(params.get("statType").toString());
        Long targetId = Long.parseLong(params.get("targetId").toString());
        Integer period = Integer.parseInt(params.get("period").toString());
        LocalDate start = LocalDate.parse(params.get("start").toString());
        LocalDate end = params.get("end") != null ? LocalDate.parse(params.get("end").toString()) : start;
        Long id = reliabilityIndexService.calculateIndex(statType, targetId, period, start, end);
        return Result.success(id);
    }

    /**
     * 趋势对比查询
     */
    @GetMapping("/compare")
    @Operation(summary = "趋势对比查询")
    public Result<List<ReliabilityIndex>> compare(
            @RequestParam Long statType,
            @RequestParam Long targetId,
            @RequestParam Integer period,
            @RequestParam String start,
            @RequestParam String end) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReliabilityIndex::getStatType, statType)
               .eq(ReliabilityIndex::getTargetId, targetId)
               .eq(ReliabilityIndex::getPeriod, period)
               .between(ReliabilityIndex::getStatDate, LocalDate.parse(start), LocalDate.parse(end))
               .orderByAsc(ReliabilityIndex::getStatDate);
        return Result.success(reliabilityIndexService.list(wrapper));
    }

    /**
     * 导出指标Excel
     */
    @PostMapping("/export")
    @Operation(summary = "导出可靠性指标Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Long statType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        if (statType != null) {
            wrapper.eq(ReliabilityIndex::getStatType, statType);
        }
        if (targetId != null) {
            wrapper.eq(ReliabilityIndex::getTargetId, targetId);
        }
        if (period != null) {
            wrapper.eq(ReliabilityIndex::getPeriod, period);
        }
        if (start != null && !start.isEmpty()) {
            wrapper.ge(ReliabilityIndex::getStatDate, LocalDate.parse(start));
        }
        if (end != null && !end.isEmpty()) {
            wrapper.le(ReliabilityIndex::getStatDate, LocalDate.parse(end));
        }
        wrapper.orderByDesc(ReliabilityIndex::getStatDate);
        List<ReliabilityIndex> list = reliabilityIndexService.list(wrapper);
        ExcelExportUtil.export(response, list, "可靠性指标导出");
    }
}
