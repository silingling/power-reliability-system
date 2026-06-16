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
import java.util.ArrayList;
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
    @PostMapping("/list")
    @Operation(summary = "分页查询可靠性指标")
    public Result<Page<ReliabilityIndex>> list(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

        Page<ReliabilityIndex> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();

        if (params.get("statType") != null && !params.get("statType").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getStatType, Long.parseLong(params.get("statType").toString()));
        }
        if (params.get("targetId") != null && !params.get("targetId").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getTargetId, Long.parseLong(params.get("targetId").toString()));
        }
        if (params.get("period") != null && !params.get("period").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getPeriod, Integer.parseInt(params.get("period").toString()));
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
    @PostMapping("/compare")
    @Operation(summary = "趋势对比查询")
    public Result<List<ReliabilityIndex>> compare(@RequestBody Map<String, Object> params) {
        Long statType = Long.parseLong(params.get("statType").toString());
        Long targetId = Long.parseLong(params.get("targetId").toString());
        Integer period = Integer.parseInt(params.get("period").toString());
        LocalDate start = LocalDate.parse(params.get("start").toString());
        LocalDate end = LocalDate.parse(params.get("end").toString());

        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReliabilityIndex::getStatType, statType)
               .eq(ReliabilityIndex::getTargetId, targetId)
               .eq(ReliabilityIndex::getPeriod, period)
               .between(ReliabilityIndex::getStatDate, start, end)
               .orderByAsc(ReliabilityIndex::getStatDate);

        return Result.success(reliabilityIndexService.list(wrapper));
    }

    /**
     * 导出指标Excel
     */
    @PostMapping("/export")
    @Operation(summary = "导出可靠性指标Excel")
    public void export(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();

        if (params.get("statType") != null && !params.get("statType").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getStatType, Long.parseLong(params.get("statType").toString()));
        }
        if (params.get("targetId") != null && !params.get("targetId").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getTargetId, Long.parseLong(params.get("targetId").toString()));
        }
        if (params.get("period") != null && !params.get("period").toString().isEmpty()) {
            wrapper.eq(ReliabilityIndex::getPeriod, Integer.parseInt(params.get("period").toString()));
        }
        if (params.get("start") != null && !params.get("start").toString().isEmpty()) {
            wrapper.ge(ReliabilityIndex::getStatDate, LocalDate.parse(params.get("start").toString()));
        }
        if (params.get("end") != null && !params.get("end").toString().isEmpty()) {
            wrapper.le(ReliabilityIndex::getStatDate, LocalDate.parse(params.get("end").toString()));
        }

        wrapper.orderByDesc(ReliabilityIndex::getStatDate);
        List<ReliabilityIndex> list = reliabilityIndexService.list(wrapper);
        ExcelExportUtil.export(response, list, "可靠性指标导出");
    }
}
