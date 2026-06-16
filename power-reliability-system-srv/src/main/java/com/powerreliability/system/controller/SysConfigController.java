package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
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

    @GetMapping("/list")
    public Result<PageResult<SysConfig>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysConfig::getConfigKey, keyword)
                    .or().like(SysConfig::getConfigDesc, keyword);
        }
        wrapper.orderByAsc(SysConfig::getId);

        IPage<SysConfig> resultPage = sysConfigService.page(new Page<>(page, pageSize), wrapper);
        return Result.ok(PageResult.of(resultPage.getRecords(), resultPage.getTotal(), page, pageSize));
    }

    @PutMapping("/update")
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

    @GetMapping("/query-system-status")
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

    @lombok.Data
    public static class ConfigUpdateRequest {
        private Long id;
        private String configKey;
        private String configValue;
        private String configDesc;
    }
}
