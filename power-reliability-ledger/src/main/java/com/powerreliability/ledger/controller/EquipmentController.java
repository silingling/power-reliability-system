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
    @PostMapping("/equipment/list")
    public Result<PageResult<Equipment>> list(@RequestBody EquipmentQuery query) {
        Page<Equipment> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getEquipmentCode())) {
            wrapper.like(Equipment::getEquipmentCode, query.getEquipmentCode());
        }
        if (StringUtils.hasText(query.getEquipmentName())) {
            wrapper.like(Equipment::getEquipmentName, query.getEquipmentName());
        }
        if (query.getEquipmentType() != null) {
            wrapper.eq(Equipment::getEquipmentType, query.getEquipmentType());
        }
        if (StringUtils.hasText(query.getAreaCode())) {
            wrapper.eq(Equipment::getAreaCode, query.getAreaCode());
        }
        if (StringUtils.hasText(query.getLineCode())) {
            wrapper.eq(Equipment::getLineCode, query.getLineCode());
        }
        if (query.getEquipmentStatus() != null) {
            wrapper.eq(Equipment::getEquipmentStatus, query.getEquipmentStatus());
        }
        wrapper.orderByDesc(Equipment::getUpdateTime);
        equipmentService.page(page, wrapper);
        PageResult<Equipment> pageResult = PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getPageSize());
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
    @PostMapping("/equipment/update")
    public Result<Void> update(@RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return Result.ok();
    }

    /**
     * 设备详情
     */
    @PostMapping("/equipment/detail/{id}")
    public Result<Equipment> detail(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        return Result.ok(equipment);
    }

    /**
     * 删除设备
     */
    @PostMapping("/equipment/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return Result.ok();
    }

    /**
     * 设备分页查询参数
     */
    public static class EquipmentQuery {
        private int page = 1;
        private int pageSize = 20;
        private String equipmentCode;
        private String equipmentName;
        private Integer equipmentType;
        private String areaCode;
        private String lineCode;
        private Integer equipmentStatus;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public String getEquipmentCode() { return equipmentCode; }
        public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
        public String getEquipmentName() { return equipmentName; }
        public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
        public Integer getEquipmentType() { return equipmentType; }
        public void setEquipmentType(Integer equipmentType) { this.equipmentType = equipmentType; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public String getLineCode() { return lineCode; }
        public void setLineCode(String lineCode) { this.lineCode = lineCode; }
        public Integer getEquipmentStatus() { return equipmentStatus; }
        public void setEquipmentStatus(Integer equipmentStatus) { this.equipmentStatus = equipmentStatus; }
    }
}
