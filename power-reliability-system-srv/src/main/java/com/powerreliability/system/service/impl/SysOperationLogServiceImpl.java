package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysOperationLog;
import com.powerreliability.system.mapper.SysOperationLogMapper;
import com.powerreliability.system.service.ISysOperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements ISysOperationLogService {

    @Override
    public void record(Long userId, String module, String action, String targetId, String params, String result, String ip) {
        SysOperationLog log = new SysOperationLog();
        log.setUserId(userId);
        log.setModule(module);
        log.setAction(action);
        log.setTargetId(targetId);
        log.setRequestParams(params);
        log.setResponseResult(result);
        log.setIpAddress(ip);
        log.setOperationTime(LocalDateTime.now());
        save(log);
    }
}
