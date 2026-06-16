package com.powerreliability.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.system.entity.SysOperationLog;

public interface ISysOperationLogService extends IService<SysOperationLog> {
    /**
     * 记录操作日志
     */
    void record(Long userId, String module, String action, String targetId, String params, String result, String ip);
}
