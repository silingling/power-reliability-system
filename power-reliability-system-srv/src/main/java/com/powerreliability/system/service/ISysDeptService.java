package com.powerreliability.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.system.entity.SysDept;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {
    /**
     * 构建部门树
     */
    List<SysDept> buildTree(List<SysDept> depts);
}
