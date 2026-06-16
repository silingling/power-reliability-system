package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysMenu;
import com.powerreliability.system.mapper.SysMenuMapper;
import com.powerreliability.system.service.ISysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> buildTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        // 按 parentId 分组
        Map<Long, List<SysMenu>> parentMap = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : 0L));

        // 为每个菜单设置子菜单
        for (SysMenu menu : menus) {
            Long id = menu.getId();
            if (parentMap.containsKey(id)) {
                List<SysMenu> children = parentMap.get(id);
                children.sort(Comparator.comparingInt(SysMenu::getSortOrder));
                menu.setChildren(children);
            } else {
                menu.setChildren(new ArrayList<>());
            }
        }

        // 返回顶级菜单（parentId == 0）
        return menus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0L)
                .sorted(Comparator.comparingInt(SysMenu::getSortOrder))
                .collect(Collectors.toList());
    }
}
