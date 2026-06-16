package com.powerreliability.index.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.index.entity.IndexAlert;
import com.powerreliability.index.service.IndexAlertService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指标异常预警控制器
 */
@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexAlertController {

    private final IndexAlertService indexAlertService;

    /**
     * 分页查询预警
     */
    @GetMapping("/alert/list")
    @Operation(summary = "分页查询指标预警")
    public Result<Page<IndexAlert>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String alertType) {
        Page<IndexAlert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IndexAlert> wrapper = new LambdaQueryWrapper<>();
        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(IndexAlert::getAlertLevel, alertLevel);
        }
        if (status != null) {
            wrapper.eq(IndexAlert::getStatus, status);
        }
        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(IndexAlert::getAlertType, alertType);
        }
        wrapper.orderByDesc(IndexAlert::getAlertTime);
        return Result.success(indexAlertService.page(page, wrapper));
    }

    /**
     * 导出预警Excel
     */
    @PostMapping("/alert/export")
    @Operation(summary = "导出预警Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String alertType) {
        LambdaQueryWrapper<IndexAlert> wrapper = new LambdaQueryWrapper<>();
        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(IndexAlert::getAlertLevel, alertLevel);
        }
        if (status != null) {
            wrapper.eq(IndexAlert::getStatus, status);
        }
        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(IndexAlert::getAlertType, alertType);
        }
        wrapper.orderByDesc(IndexAlert::getAlertTime);
        List<IndexAlert> list = indexAlertService.list(wrapper);
        ExcelExportUtil.export(response, list, "指标预警导出");
    }

    /**
     * 触发阈值检查
     */
    @PostMapping("/alert/check")
    @Operation(summary = "触发阈值检查")
    public Result<Integer> check() {
        int count = indexAlertService.checkThresholds();
        return Result.success(count);
    }

    /**
     * 处理预警
     */
    @PostMapping("/alert/handle/{id}")
    @Operation(summary = "处理预警")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String measures = params.get("measures") != null ? params.get("measures").toString() : "";
        try {
            indexAlertService.handleAlert(id, measures);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
