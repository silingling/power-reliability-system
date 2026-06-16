package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.service.ITrAreaService;
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
    @PostMapping("/area/list")
    public Result<PageResult<TrArea>> list(@RequestBody TrAreaQuery query) {
        Page<TrArea> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getAreaCode())) {
            wrapper.like(TrArea::getAreaCode, query.getAreaCode());
        }
        if (StringUtils.hasText(query.getAreaName())) {
            wrapper.like(TrArea::getAreaName, query.getAreaName());
        }
        if (StringUtils.hasText(query.getPowerSupplyStation())) {
            wrapper.eq(TrArea::getPowerSupplyStation, query.getPowerSupplyStation());
        }
        if (StringUtils.hasText(query.getMaintenanceTeam())) {
            wrapper.eq(TrArea::getMaintenanceTeam, query.getMaintenanceTeam());
        }
        if (query.getAreaStatus() != null) {
            wrapper.eq(TrArea::getAreaStatus, query.getAreaStatus());
        }
        wrapper.orderByDesc(TrArea::getUpdateTime);
        trAreaService.page(page, wrapper);
        PageResult<TrArea> pageResult = PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getPageSize());
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
    @PostMapping("/area/update")
    public Result<Void> update(@RequestBody TrArea trArea) {
        trAreaService.updateById(trArea);
        return Result.ok();
    }

    /**
     * 台区详情
     */
    @PostMapping("/area/detail/{id}")
    public Result<TrArea> detail(@PathVariable Long id) {
        TrArea trArea = trAreaService.getById(id);
        return Result.ok(trArea);
    }

    /**
     * 删除台区
     */
    @PostMapping("/area/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trAreaService.removeById(id);
        return Result.ok();
    }

    /**
     * 导出台区
     */
    @PostMapping("/area/export")
    public Result<List<TrArea>> export(@RequestBody TrAreaQuery query) {
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getAreaCode())) {
            wrapper.like(TrArea::getAreaCode, query.getAreaCode());
        }
        if (StringUtils.hasText(query.getAreaName())) {
            wrapper.like(TrArea::getAreaName, query.getAreaName());
        }
        if (StringUtils.hasText(query.getPowerSupplyStation())) {
            wrapper.eq(TrArea::getPowerSupplyStation, query.getPowerSupplyStation());
        }
        if (StringUtils.hasText(query.getMaintenanceTeam())) {
            wrapper.eq(TrArea::getMaintenanceTeam, query.getMaintenanceTeam());
        }
        if (query.getAreaStatus() != null) {
            wrapper.eq(TrArea::getAreaStatus, query.getAreaStatus());
        }
        wrapper.orderByDesc(TrArea::getUpdateTime);
        List<TrArea> list = trAreaService.list(wrapper);
        return Result.ok(list);
    }

    /**
     * 台区分页查询参数
     */
    public static class TrAreaQuery {
        private int page = 1;
        private int pageSize = 20;
        private String areaCode;
        private String areaName;
        private String powerSupplyStation;
        private String maintenanceTeam;
        private Integer areaStatus;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public String getPowerSupplyStation() { return powerSupplyStation; }
        public void setPowerSupplyStation(String powerSupplyStation) { this.powerSupplyStation = powerSupplyStation; }
        public String getMaintenanceTeam() { return maintenanceTeam; }
        public void setMaintenanceTeam(String maintenanceTeam) { this.maintenanceTeam = maintenanceTeam; }
        public Integer getAreaStatus() { return areaStatus; }
        public void setAreaStatus(Integer areaStatus) { this.areaStatus = areaStatus; }
    }
}
