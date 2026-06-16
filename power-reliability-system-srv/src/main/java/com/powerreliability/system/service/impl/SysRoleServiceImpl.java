package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysMenu;
import com.powerreliability.system.entity.SysRole;
import com.powerreliability.system.entity.SysRoleMenu;
import com.powerreliability.system.entity.SysUserRole;
import com.powerreliability.system.mapper.SysRoleMapper;
import com.powerreliability.system.service.ISysMenuService;
import com.powerreliability.system.service.ISysRoleMenuService;
import com.powerreliability.system.service.ISysRoleService;
import com.powerreliability.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 查询用户的角色
        List<SysUserRole> userRoles = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        // 查询角色关联的菜单
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());

        // 查询菜单权限标识
        List<SysMenu> menus = sysMenuService.listByIds(menuIds);
        return menus.stream()
                .map(SysMenu::getPermissionKey)
                .filter(k -> k != null && !k.isEmpty())
                .collect(Collectors.toList());
    }
}
