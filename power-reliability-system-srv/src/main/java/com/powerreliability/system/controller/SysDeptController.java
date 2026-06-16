package com.powerreliability.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.DeptAddRequest;
import com.powerreliability.system.entity.SysDept;
import com.powerreliability.system.service.ISysDeptService;
import com.powerreliability.system.service.ISysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/dept")
public class SysDeptController {

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @PostMapping("/tree")
    public Result<List<SysDept>> tree() {
        List<SysDept> allDepts = sysDeptService.list(
                new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSortOrder));
        List<SysDept> tree = sysDeptService.buildTree(allDepts);
        return Result.ok(tree);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody DeptAddRequest request, HttpServletRequest httpRequest) {
        SysDept dept = new SysDept();
        dept.setDeptName(request.getDeptName());
        dept.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        dept.setDeptType(request.getDeptType());
        dept.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        dept.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        sysDeptService.save(dept);

        sysOperationLogService.record(0L, "系统管理", "新增部门",
                dept.getId().toString(), request.getDeptName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody SysDept dept, HttpServletRequest httpRequest) {
        sysDeptService.updateById(dept);
        sysOperationLogService.record(0L, "系统管理", "更新部门",
                dept.getId().toString(), dept.getDeptName(), "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        // 检查是否有子部门
        List<SysDept> children = sysDeptService.list(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (!children.isEmpty()) {
            return Result.fail("存在子部门，无法删除");
        }
        sysDeptService.removeById(id);
        sysOperationLogService.record(0L, "系统管理", "删除部门",
                id.toString(), "", "成功", httpRequest.getRemoteAddr());
        return Result.ok();
    }
}
