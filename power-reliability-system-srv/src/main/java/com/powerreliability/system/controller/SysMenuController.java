package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.MenuAddRequest;
import com.powerreliability.system.entity.SysMenu;
import com.powerreliability.system.service.ISysMenuService;
import com.powerreliability.system.service.ISysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @PostMapping("/tree")
    public Result<List<SysMenu>> tree() {
        List<SysMenu> allMenus = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        List<SysMenu> tree = sysMenuService.buildTree(allMenus);
        return Result.ok(tree);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody MenuAddRequest request, HttpServletRequest httpRequest) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(request.getMenuName());
        menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        menu.setMenuType(request.getMenuType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermissionKey(request.getPermissionKey());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menu.setVisible(request.getVisible() != null ? request.getVisible() : 1);
        sysMenuService.save(menu);

        sysOperationLogService.record(0L, "系统管理", "新增菜单",
                menu.getId().toString(), request.getMenuName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody SysMenu menu, HttpServletRequest httpRequest) {
        sysMenuService.updateById(menu);
        sysOperationLogService.record(0L, "系统管理", "更新菜单",
                menu.getId().toString(), menu.getMenuName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        // 检查是否有子菜单
        List<SysMenu> children = sysMenuService.list(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (!children.isEmpty()) {
            return Result.fail("存在子菜单，无法删除");
        }
        sysMenuService.removeById(id);
        sysOperationLogService.record(0L, "系统管理", "删除菜单",
                id.toString(), "", "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }
}
