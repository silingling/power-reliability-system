package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.OutageExemption;
import com.powerreliability.outage.service.OutageExemptionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/outage/exemption")
public class OutageExemptionController {

    @Autowired
    private OutageExemptionService outageExemptionService;

    @GetMapping("/list")
    @Operation(summary = "分页查询豁免停电列表")
    public Result<PageResult<OutageExemption>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<OutageExemption> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OutageExemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OutageExemption::getCreateTime);

        Page<OutageExemption> result = outageExemptionService.page(pageParam, wrapper);
        PageResult<OutageExemption> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出豁免停电Excel")
    public void export(HttpServletResponse response) {
        LambdaQueryWrapper<OutageExemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OutageExemption::getCreateTime);
        List<OutageExemption> list = outageExemptionService.list(wrapper);
        ExcelExportUtil.export(response, list, "豁免停电导出");
    }

    @PostMapping("/auto-verdict/{eventId}")
    @Operation(summary = "自动判责")
    public Result<OutageExemption> autoVerdict(@PathVariable Long eventId) {
        try {
            OutageExemption exemption = outageExemptionService.autoVerdict(eventId);
            return Result.ok(exemption);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/manual")
    @Operation(summary = "人工判责")
    public Result<Void> manual(@RequestBody OutageExemption exemption) {
        exemption.setVerdictTime(LocalDateTime.now());
        if (exemption.getId() != null) {
            outageExemptionService.updateById(exemption);
        } else {
            outageExemptionService.save(exemption);
        }
        return Result.ok();
    }
}
