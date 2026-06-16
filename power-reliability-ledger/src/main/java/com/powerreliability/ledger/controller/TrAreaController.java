package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.service.ITrAreaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class TrAreaController {

    @Autowired
    private ITrAreaService trAreaService;

    /**
     * 分页查询台区
     */
    @GetMapping("/area/list")
    public Result<PageResult<TrArea>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) String powerSupplyStation,
            @RequestParam(required = false) String maintenanceTeam,
            @RequestParam(required = false) Integer areaStatus) {
        Page<TrArea> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(areaCode)) {
            wrapper.like(TrArea::getAreaCode, areaCode);
        }
        if (StringUtils.hasText(areaName)) {
            wrapper.like(TrArea::getAreaName, areaName);
        }
        if (StringUtils.hasText(powerSupplyStation)) {
            wrapper.eq(TrArea::getPowerSupplyStation, powerSupplyStation);
        }
        if (StringUtils.hasText(maintenanceTeam)) {
            wrapper.eq(TrArea::getMaintenanceTeam, maintenanceTeam);
        }
        if (areaStatus != null) {
            wrapper.eq(TrArea::getAreaStatus, areaStatus);
        }
        wrapper.orderByDesc(TrArea::getUpdateTime);
        trAreaService.page(pageParam, wrapper);
        PageResult<TrArea> pageResult = PageResult.of(pageParam.getRecords(), pageParam.getTotal(), page, pageSize);
        return Result.ok(pageResult);
    }

    /**
     * 新增台区
     */
    @PostMapping("/area/add")
    public Result<Void> add(@RequestBody TrArea trArea) {
        trAreaService.save(trArea);
        return Result.ok();
    }

    /**
     * 更新台区
     */
    @PutMapping("/area/update")
    public Result<Void> update(@RequestBody TrArea trArea) {
        trAreaService.updateById(trArea);
        return Result.ok();
    }

    /**
     * 台区详情
     */
    @GetMapping("/area/detail/{id}")
    public Result<TrArea> detail(@PathVariable Long id) {
        TrArea trArea = trAreaService.getById(id);
        return Result.ok(trArea);
    }

    /**
     * 删除台区
     */
    @DeleteMapping("/area/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trAreaService.removeById(id);
        return Result.ok();
    }

    /**
     * 导出台区
     */
    @GetMapping("/area/export")
    public Result<List<TrArea>> export(
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaName,
            @RequestParam(required = false) String powerSupplyStation,
            @RequestParam(required = false) String maintenanceTeam,
            @RequestParam(required = false) Integer areaStatus) {
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(areaCode)) {
            wrapper.like(TrArea::getAreaCode, areaCode);
        }
        if (StringUtils.hasText(areaName)) {
            wrapper.like(TrArea::getAreaName, areaName);
        }
        if (StringUtils.hasText(powerSupplyStation)) {
            wrapper.eq(TrArea::getPowerSupplyStation, powerSupplyStation);
        }
        if (StringUtils.hasText(maintenanceTeam)) {
            wrapper.eq(TrArea::getMaintenanceTeam, maintenanceTeam);
        }
        if (areaStatus != null) {
            wrapper.eq(TrArea::getAreaStatus, areaStatus);
        }
        wrapper.orderByDesc(TrArea::getUpdateTime);
        List<TrArea> list = trAreaService.list(wrapper);
        return Result.ok(list);
    }
}
