package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.FaultOutage;
import com.powerreliability.outage.service.FaultOutageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/outage/fault")
public class FaultOutageController {

    @Autowired
    private FaultOutageService faultOutageService;

    @GetMapping("/list")
    @Operation(summary = "分页查询故障停电列表")
    public Result<PageResult<FaultOutage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer faultLevel,
            @RequestParam(required = false) Integer faultSource) {
        Page<FaultOutage> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<FaultOutage> wrapper = new LambdaQueryWrapper<>();

        if (faultLevel != null) {
            wrapper.eq(FaultOutage::getFaultLevel, faultLevel);
        }
        if (faultSource != null) {
            wrapper.eq(FaultOutage::getFaultSource, faultSource);
        }

        wrapper.orderByDesc(FaultOutage::getCreateTime);
        Page<FaultOutage> result = faultOutageService.page(pageParam, wrapper);
        PageResult<FaultOutage> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出故障停电Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer faultLevel,
            @RequestParam(required = false) Integer faultSource) {
        LambdaQueryWrapper<FaultOutage> wrapper = new LambdaQueryWrapper<>();
        if (faultLevel != null) {
            wrapper.eq(FaultOutage::getFaultLevel, faultLevel);
        }
        if (faultSource != null) {
            wrapper.eq(FaultOutage::getFaultSource, faultSource);
        }
        wrapper.orderByDesc(FaultOutage::getCreateTime);
        List<FaultOutage> list = faultOutageService.list(wrapper);
        ExcelExportUtil.export(response, list, "故障停电导出");
    }

    @PostMapping("/report")
    @Operation(summary = "上报故障停电")
    public Result<Void> report(@RequestBody FaultOutage faultOutage) {
        faultOutage.setFaultTime(LocalDateTime.now());
        faultOutage.setFaultSource(2); // 人工上报
        if (faultOutage.getFaultLevel() == null) {
            faultOutage.setFaultLevel(1); // 默认一般
        }
        faultOutageService.save(faultOutage);
        return Result.ok();
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询故障停电详情")
    public Result<FaultOutage> detail(@PathVariable Long id) {
        FaultOutage faultOutage = faultOutageService.getById(id);
        if (faultOutage == null) {
            return Result.fail("故障停电记录不存在");
        }
        return Result.ok(faultOutage);
    }

    @PutMapping("/update")
    @Operation(summary = "更新故障停电记录")
    public Result<Void> update(@RequestBody FaultOutage faultOutage) {
        if (faultOutage.getId() == null) {
            return Result.fail("ID不能为空");
        }
        faultOutageService.updateById(faultOutage);
        return Result.ok();
    }
}
