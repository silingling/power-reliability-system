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
import java.util.Map;

@RestController
@RequestMapping("/api/outage")
@Tag(name = "停电事件管理")
public class OutageEventController {

    @Autowired
    private OutageEventService outageEventService;

    @PostMapping("/event/list")
    @Operation(summary = "分页查询停电事件")
    public Result<PageResult<OutageEvent>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<OutageEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OutageEvent> wrapper = buildQueryWrapper(params);

        Page<OutageEvent> result = outageEventService.page(pageParam, wrapper);

        PageResult<OutageEvent> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/event/export")
    @Operation(summary = "导出停电事件Excel")
    public void export(@RequestBody Map<String, Object> params, HttpServletResponse response) {
        LambdaQueryWrapper<OutageEvent> wrapper = buildQueryWrapper(params);
        wrapper.orderByDesc(OutageEvent::getCreateTime);
        List<OutageEvent> list = outageEventService.list(wrapper);
        ExcelExportUtil.export(response, list, "停电事件导出");
    }

    @PostMapping("/event/detail/{id}")
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

    @PostMapping("/event/update")
    @Operation(summary = "更新停电事件")
    public Result<Void> update(@RequestBody OutageEvent event) {
        outageEventService.updateById(event);
        return Result.ok();
    }

    /**
     * 构建通用查询条件
     */
    private LambdaQueryWrapper<OutageEvent> buildQueryWrapper(Map<String, Object> params) {
        LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();

        if (params.get("outageType") != null && !params.get("outageType").toString().isEmpty()) {
            wrapper.eq(OutageEvent::getOutageType, Integer.parseInt(params.get("outageType").toString()));
        }
        if (params.get("areaCode") != null && !params.get("areaCode").toString().isEmpty()) {
            wrapper.like(OutageEvent::getAreaCode, params.get("areaCode").toString());
        }
        if (params.get("areaName") != null && !params.get("areaName").toString().isEmpty()) {
            wrapper.like(OutageEvent::getAreaName, params.get("areaName").toString());
        }
        if (params.get("eventNo") != null && !params.get("eventNo").toString().isEmpty()) {
            wrapper.like(OutageEvent::getEventNo, params.get("eventNo").toString());
        }
        if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
            wrapper.ge(OutageEvent::getOutageStartTime, LocalDateTime.parse(params.get("startTime").toString()));
        }
        if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
            wrapper.le(OutageEvent::getOutageStartTime, LocalDateTime.parse(params.get("endTime").toString()));
        }
        if (params.get("isClosed") != null && !params.get("isClosed").toString().isEmpty()) {
            wrapper.eq(OutageEvent::getIsClosed, Integer.parseInt(params.get("isClosed").toString()));
        }
        if (params.get("isExempt") != null && !params.get("isExempt").toString().isEmpty()) {
            wrapper.eq(OutageEvent::getIsExempt, Integer.parseInt(params.get("isExempt").toString()));
        }

        wrapper.orderByDesc(OutageEvent::getCreateTime);
        return wrapper;
    }
}
