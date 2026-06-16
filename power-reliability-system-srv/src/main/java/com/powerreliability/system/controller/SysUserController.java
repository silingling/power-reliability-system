package com.powerreliability.system.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.ResetPasswordRequest;
import com.powerreliability.system.dto.UserAddRequest;
import com.powerreliability.system.entity.SysUser;
import com.powerreliability.system.entity.SysUserRole;
import com.powerreliability.system.service.ISysOperationLogService;
import com.powerreliability.system.service.ISysUserRoleService;
import com.powerreliability.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> resultPage = sysUserService.page(new Page<>(page, pageSize), wrapper);
        resultPage.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.of(resultPage.getRecords(), resultPage.getTotal(), page, pageSize));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody UserAddRequest request, HttpServletRequest httpRequest) {
        SysUser existUser = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtil.sha256Hex(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setPost(request.getPost());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        sysUserService.save(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                sysUserRoleService.save(ur);
            }
        }

        sysOperationLogService.record(request.hashCode() & 0x7FFFFFFF,
                "系统管理", "新增用户", user.getId().toString(),
                request.getUsername(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysUser user, HttpServletRequest httpRequest) {
        user.setPassword(null);
        sysUserService.updateById(user);
        sysOperationLogService.record(user.getId(),
                "系统管理", "更新用户", user.getId().toString(),
                user.getUsername(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @GetMapping("/detail/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        List<SysUserRole> userRoles = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return Result.ok(user);
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        SysUser user = sysUserService.getById(request.getUserId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(DigestUtil.sha256Hex(request.getNewPassword()));
        sysUserService.updateById(user);
        sysOperationLogService.record(request.getUserId(),
                "系统管理", "重置密码", request.getUserId().toString(),
                user.getUsername(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }
}
