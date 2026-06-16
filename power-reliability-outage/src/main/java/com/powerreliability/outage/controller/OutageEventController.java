package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.service.OutageEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/outage")
@Tag(name = "停电事件管理")
public class OutageEventController {

    @Autowired
    private OutageEventService outageEventService;

    @GetMapping("/event/list")
    @Operation(summary = "分页查询停电事件")
    public Result<PageResult<OutageEvent>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer outageType,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) String eventNo,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer isClosed,
            @RequestParam(required = false) Integer isExempt) {
        Page<OutageEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OutageEvent> wrapper = buildQueryWrapper(outageType, areaCode, areaName, eventNo, startTime, endTime, isClosed, isExempt);
        Page<OutageEvent> result = outageEventService.page(pageParam, wrapper);
        PageResult<OutageEvent> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @GetMapping("/event/export")
    @Operation(summary = "导出停电事件Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Integer outageType,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) String eventNo,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer isClosed,
            @RequestParam(required = false) Integer isExempt) {
        LambdaQueryWrapper<OutageEvent> wrapper = buildQueryWrapper(outageType, areaCode, areaName, eventNo, startTime, endTime, isClosed, isExempt);
        wrapper.orderByDesc(OutageEvent::getCreateTime);
        List<OutageEvent> list = outageEventService.list(wrapper);
        ExcelExportUtil.export(response, list, "停电事件导出");
    }

    @GetMapping("/event/detail/{id}")
    @Operation(summary = "查询停电事件详情")
    public Result<OutageEvent> detail(@PathVariable Long id) {
        OutageEvent event = outageEventService.getById(id);
        if (event == null) {
            return Result.fail("停电事件不存在");
        }
        return Result.ok(event);
    }

    @PostMapping("/event/create")
    @Operation(summary = "新增停电事件")
    public Result<Void> create(@RequestBody OutageEvent event) {
        outageEventService.save(event);
        return Result.ok();
    }

    @PutMapping("/event/update")
    @Operation(summary = "更新停电事件")
    public Result<Void> update(@RequestBody OutageEvent event) {
        outageEventService.updateById(event);
        return Result.ok();
    }

    /**
     * 构建通用查询条件
     */
    private LambdaQueryWrapper<OutageEvent> buildQueryWrapper(
            Integer outageType, String areaCode, String areaName, String eventNo,
            String startTime, String endTime, Integer isClosed, Integer isExempt) {
        LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();

        if (outageType != null) {
            wrapper.eq(OutageEvent::getOutageType, outageType);
        }
        if (areaCode != null && !areaCode.isEmpty()) {
            wrapper.like(OutageEvent::getAreaCode, areaCode);
        }
        if (areaName != null && !areaName.isEmpty()) {
            wrapper.like(OutageEvent::getAreaName, areaName);
        }
        if (eventNo != null && !eventNo.isEmpty()) {
            wrapper.like(OutageEvent::getEventNo, eventNo);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(OutageEvent::getOutageStartTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(OutageEvent::getOutageStartTime, LocalDateTime.parse(endTime));
        }
        if (isClosed != null) {
            wrapper.eq(OutageEvent::getIsClosed, isClosed);
        }
        if (isExempt != null) {
            wrapper.eq(OutageEvent::getIsExempt, isExempt);
        }

        wrapper.orderByDesc(OutageEvent::getCreateTime);
        return wrapper;
    }
}
