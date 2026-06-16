package com.powerreliability.system.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysUser;
import com.powerreliability.system.mapper.SysUserMapper;
import com.powerreliability.system.service.ISysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Override
    public SysUser login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getStatus, 1);
        SysUser user = getOne(wrapper);
        if (user == null) {
            return null;
        }
        // 使用SHA256验证密码（实际生产可替换为BCrypt）
        String encryptedPwd = DigestUtil.sha256Hex(password);
        if (!user.getPassword().equals(encryptedPwd)) {
            return null;
        }
        return user;
    }
}
