package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.RoleAddRequest;
import com.powerreliability.system.dto.RolePermissionRequest;
import com.powerreliability.system.entity.SysRole;
import com.powerreliability.system.entity.SysRoleMenu;
import com.powerreliability.system.service.ISysOperationLogService;
import com.powerreliability.system.service.ISysRoleMenuService;
import com.powerreliability.system.service.ISysRoleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @GetMapping("/list")
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getRoleKey, keyword);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getRoleSort);

        IPage<SysRole> resultPage = sysRoleService.page(new Page<>(page, pageSize), wrapper);
        return Result.ok(PageResult.of(resultPage.getRecords(), resultPage.getTotal(), page, pageSize));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody RoleAddRequest request, HttpServletRequest httpRequest) {
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName());
        role.setRoleKey(request.getRoleKey());
        role.setRoleSort(request.getRoleSort() != null ? request.getRoleSort() : 0);
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        sysRoleService.save(role);

        sysOperationLogService.record(0L, "系统管理", "新增角色",
                role.getId().toString(), request.getRoleName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysRole role, HttpServletRequest httpRequest) {
        sysRoleService.updateById(role);
        sysOperationLogService.record(0L, "系统管理", "更新角色",
                role.getId().toString(), role.getRoleName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        sysRoleService.removeById(id);
        sysRoleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        sysOperationLogService.record(0L, "系统管理", "删除角色",
                id.toString(), "", "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/permission")
    public Result<Void> assignPermission(@RequestBody RolePermissionRequest request, HttpServletRequest httpRequest) {
        sysRoleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, request.getRoleId()));

        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            for (Long menuId : request.getMenuIds()) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(request.getRoleId());
                rm.setMenuId(menuId);
                sysRoleMenuService.save(rm);
            }
        }

        sysOperationLogService.record(0L, "系统管理", "分配权限",
                request.getRoleId().toString(), "", "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }
}
