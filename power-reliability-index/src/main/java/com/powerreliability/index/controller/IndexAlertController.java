package com.powerreliability.index.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.index.entity.IndexAlert;
import com.powerreliability.index.service.IndexAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/alert/list")
    public Result<Page<IndexAlert>> list(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

        Page<IndexAlert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IndexAlert> wrapper = new LambdaQueryWrapper<>();

        if (params.get("alertLevel") != null && !params.get("alertLevel").toString().isEmpty()) {
            wrapper.eq(IndexAlert::getAlertLevel, params.get("alertLevel").toString());
        }
        if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
            wrapper.eq(IndexAlert::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        if (params.get("alertType") != null && !params.get("alertType").toString().isEmpty()) {
            wrapper.eq(IndexAlert::getAlertType, params.get("alertType").toString());
        }

        wrapper.orderByDesc(IndexAlert::getAlertTime);
        return Result.success(indexAlertService.page(page, wrapper));
    }

    /**
     * 触发阈值检查
     */
    @PostMapping("/alert/check")
    public Result<Integer> check() {
        int count = indexAlertService.checkThresholds();
        return Result.success(count);
    }

    /**
     * 处理预警
     */
    @PostMapping("/alert/handle/{id}")
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
