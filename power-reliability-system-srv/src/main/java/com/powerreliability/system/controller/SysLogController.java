package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.entity.SysOperationLog;
import com.powerreliability.system.service.ISysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/system/log")
public class SysLogController {

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @GetMapping("/list")
    public Result<PageResult<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysOperationLog::getModule, module);
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(SysOperationLog::getAction, action);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(SysOperationLog::getUsername, username);
        }
        if (startTime != null) {
            wrapper.ge(SysOperationLog::getOperationTime, LocalDateTime.parse(startTime));
        }
        if (endTime != null) {
            wrapper.le(SysOperationLog::getOperationTime, LocalDateTime.parse(endTime));
        }
        wrapper.orderByDesc(SysOperationLog::getOperationTime);

        IPage<SysOperationLog> resultPage = sysOperationLogService.page(new Page<>(page, pageSize), wrapper);
        return Result.ok(PageResult.of(resultPage.getRecords(), resultPage.getTotal(), page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<SysOperationLog> detail(@PathVariable Long id) {
        SysOperationLog log = sysOperationLogService.getById(id);
        if (log == null) {
            return Result.fail("日志不存在");
        }
        return Result.ok(log);
    }
}
