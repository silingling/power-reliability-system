package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.LogQuery;
import com.powerreliability.system.entity.SysOperationLog;
import com.powerreliability.system.service.ISysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/log")
public class SysLogController {

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @PostMapping("/list")
    public Result<PageResult<SysOperationLog>> list(@RequestBody LogQuery query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getModule())) {
            wrapper.eq(SysOperationLog::getModule, query.getModule());
        }
        if (StringUtils.hasText(query.getAction())) {
            wrapper.eq(SysOperationLog::getAction, query.getAction());
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(SysOperationLog::getUsername, query.getUsername());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(SysOperationLog::getOperationTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(SysOperationLog::getOperationTime, query.getEndTime());
        }
        wrapper.orderByDesc(SysOperationLog::getOperationTime);

        IPage<SysOperationLog> page = sysOperationLogService.page(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getPageSize()));
    }

    @PostMapping("/detail/{id}")
    public Result<SysOperationLog> detail(@PathVariable Long id) {
        SysOperationLog log = sysOperationLogService.getById(id);
        if (log == null) {
            return Result.fail("日志不存在");
        }
        return Result.ok(log);
    }
}
