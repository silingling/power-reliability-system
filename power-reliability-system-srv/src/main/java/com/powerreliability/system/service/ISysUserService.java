package com.powerreliability.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.system.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {
    /**
     * 用户登录，验证用户名密码，返回用户对象
     */
    SysUser login(String username, String password);
}
