package com.powerreliability.outage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.outage.entity.FaultOutage;
import com.powerreliability.outage.service.FaultOutageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/outage/fault")
public class FaultOutageController {

    @Autowired
    private FaultOutageService faultOutageService;

    @PostMapping("/list")
    public Result<PageResult<FaultOutage>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;

        Page<FaultOutage> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<FaultOutage> wrapper = new LambdaQueryWrapper<>();

        if (params.get("faultLevel") != null && !params.get("faultLevel").toString().isEmpty()) {
            wrapper.eq(FaultOutage::getFaultLevel, Integer.parseInt(params.get("faultLevel").toString()));
        }
        if (params.get("faultSource") != null && !params.get("faultSource").toString().isEmpty()) {
            wrapper.eq(FaultOutage::getFaultSource, Integer.parseInt(params.get("faultSource").toString()));
        }

        wrapper.orderByDesc(FaultOutage::getCreateTime);
        Page<FaultOutage> result = faultOutageService.page(pageParam, wrapper);
        PageResult<FaultOutage> pageResult = PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/report")
    public Result<Void> report(@RequestBody FaultOutage faultOutage) {
        faultOutage.setFaultTime(LocalDateTime.now());
        faultOutage.setFaultSource(2); // 人工上报
        if (faultOutage.getFaultLevel() == null) {
            faultOutage.setFaultLevel(1); // 默认一般
        }
        faultOutageService.save(faultOutage);
        return Result.ok();
    }

    @PostMapping("/detail/{id}")
    public Result<FaultOutage> detail(@PathVariable Long id) {
        FaultOutage faultOutage = faultOutageService.getById(id);
        if (faultOutage == null) {
            return Result.fail("故障停电记录不存在");
        }
        return Result.ok(faultOutage);
    }
}
