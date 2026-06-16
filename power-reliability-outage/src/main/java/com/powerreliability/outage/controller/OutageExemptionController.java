package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.outage.entity.OutageExemption;
import com.powerreliability.outage.service.OutageExemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/exemption")
public class OutageExemptionController {

    @Autowired
    private OutageExemptionService outageExemptionService;

    @PostMapping("/list")
    public Result<PageResult<OutageExemption>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<OutageExemption> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OutageExemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OutageExemption::getCreateTime);

        Page<OutageExemption> result = outageExemptionService.page(pageParam, wrapper);
        PageResult<OutageExemption> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/auto-verdict/{eventId}")
    public Result<OutageExemption> autoVerdict(@PathVariable Long eventId) {
        try {
            OutageExemption exemption = outageExemptionService.autoVerdict(eventId);
            return Result.ok(exemption);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/manual")
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
