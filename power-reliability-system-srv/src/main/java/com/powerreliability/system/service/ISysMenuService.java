package com.powerreliability.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.system.entity.SysMenu;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {
    /**
     * 构建菜单树
     */
    List<SysMenu> buildTree(List<SysMenu> menus);
}
