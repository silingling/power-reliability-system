package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.ConfigQuery;
import com.powerreliability.system.dto.ConfigUpdateRequest;
import com.powerreliability.system.entity.SysConfig;
import com.powerreliability.system.service.ISysConfigService;
import com.powerreliability.system.service.ISysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/config")
public class SysConfigController {

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @PostMapping("/list")
    public Result<PageResult<SysConfig>> list(@RequestBody ConfigQuery query) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysConfig::getConfigKey, query.getKeyword())
                    .or().like(SysConfig::getConfigDesc, query.getKeyword());
        }
        wrapper.orderByAsc(SysConfig::getId);

        IPage<SysConfig> page = sysConfigService.page(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        return Result.ok(PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getPageSize()));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody ConfigUpdateRequest request, HttpServletRequest httpRequest) {
        SysConfig config = sysConfigService.getById(request.getId());
        if (config == null) {
            return Result.fail("配置不存在");
        }
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigDesc(request.getConfigDesc());
        sysConfigService.updateById(config);

        sysOperationLogService.record(0L, "系统管理", "更新配置",
                request.getId().toString(), request.getConfigKey(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/query-system-status")
    public Result<Map<String, Object>> querySystemStatus() {
        List<SysConfig> configs = sysConfigService.list();
        Map<String, Object> statusMap = new HashMap<>();
        for (SysConfig config : configs) {
            statusMap.put(config.getConfigKey(), config.getConfigValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("configs", statusMap);
        result.put("version", "1.0.0");
        result.put("status", "running");
        return Result.ok(result);
    }
}
