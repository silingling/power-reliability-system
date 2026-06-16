package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.ledger.entity.Equipment;
import com.powerreliability.ledger.service.IEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
public class EquipmentController {

    @Autowired
    private IEquipmentService equipmentService;

    /**
     * 分页查询设备
     */
    @GetMapping("/equipment/list")
    public Result<PageResult<Equipment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String equipmentCode,
            @RequestParam(required = false) String equipmentName,
            @RequestParam(required = false) Integer equipmentType,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String lineCode,
            @RequestParam(required = false) Integer equipmentStatus) {
        Page<Equipment> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(equipmentCode)) {
            wrapper.like(Equipment::getEquipmentCode, equipmentCode);
        }
        if (StringUtils.hasText(equipmentName)) {
            wrapper.like(Equipment::getEquipmentName, equipmentName);
        }
        if (equipmentType != null) {
            wrapper.eq(Equipment::getEquipmentType, equipmentType);
        }
        if (StringUtils.hasText(areaCode)) {
            wrapper.eq(Equipment::getAreaCode, areaCode);
        }
        if (StringUtils.hasText(lineCode)) {
            wrapper.eq(Equipment::getLineCode, lineCode);
        }
        if (equipmentStatus != null) {
            wrapper.eq(Equipment::getEquipmentStatus, equipmentStatus);
        }
        wrapper.orderByDesc(Equipment::getUpdateTime);
        equipmentService.page(pageParam, wrapper);
        PageResult<Equipment> pageResult = PageResult.of(pageParam.getRecords(), pageParam.getTotal(), page, pageSize);
        return Result.ok(pageResult);
    }

    /**
     * 新增设备
     */
    @PostMapping("/equipment/add")
    public Result<Void> add(@RequestBody Equipment equipment) {
        equipmentService.save(equipment);
        return Result.ok();
    }

    /**
     * 更新设备
     */
    @PutMapping("/equipment/update")
    public Result<Void> update(@RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return Result.ok();
    }

    /**
     * 设备详情
     */
    @GetMapping("/equipment/detail/{id}")
    public Result<Equipment> detail(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        return Result.ok(equipment);
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/equipment/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return Result.ok();
    }
}
