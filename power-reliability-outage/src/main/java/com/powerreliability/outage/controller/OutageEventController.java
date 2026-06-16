package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.service.OutageEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/outage")
public class OutageEventController {

    @Autowired
    private OutageEventService outageEventService;

    @PostMapping("/event/list")
    public Result<PageResult<OutageEvent>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<OutageEvent> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();

        if (params.get("outageType") != null && !params.get("outageType").toString().isEmpty()) {
            wrapper.eq(OutageEvent::getOutageType, Integer.parseInt(params.get("outageType").toString()));
        }
        if (params.get("areaCode") != null && !params.get("areaCode").toString().isEmpty()) {
            wrapper.like(OutageEvent::getAreaCode, params.get("areaCode").toString());
        }
        if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
            wrapper.ge(OutageEvent::getOutageStartTime, LocalDateTime.parse(params.get("startTime").toString()));
        }
        if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
            wrapper.le(OutageEvent::getOutageStartTime, LocalDateTime.parse(params.get("endTime").toString()));
        }

        wrapper.orderByDesc(OutageEvent::getCreateTime);
        Page<OutageEvent> result = outageEventService.page(pageParam, wrapper);

        PageResult<OutageEvent> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/event/detail/{id}")
    public Result<OutageEvent> detail(@PathVariable Long id) {
        OutageEvent event = outageEventService.getById(id);
        if (event == null) {
            return Result.fail("停电事件不存在");
        }
        return Result.ok(event);
    }

    @PostMapping("/event/create")
    public Result<Void> create(@RequestBody OutageEvent event) {
        outageEventService.save(event);
        return Result.ok();
    }

    @PostMapping("/event/update")
    public Result<Void> update(@RequestBody OutageEvent event) {
        outageEventService.updateById(event);
        return Result.ok();
    }
}
