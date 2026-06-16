package com.powerreliability.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.system.entity.SysRole;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {
    /**
     * 获取用户的所有权限标识
     */
    List<String> getUserPermissions(Long userId);
}
