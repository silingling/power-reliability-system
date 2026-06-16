package com.powerreliability.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.ledger.entity.TrArea;

import java.util.List;

public interface ITrAreaService extends IService<TrArea> {

    /**
     * 根据状态查询台区列表
     */
    List<TrArea> findByStatus(Integer areaStatus);

    /**
     * 批量更新台区状态
     */
    int batchUpdateStatus(List<Long> ids, Integer areaStatus);

    /**
     * 根据供电站查询台区
     */
    List<TrArea> findByPowerSupplyStation(String powerSupplyStation);

    /**
     * 根据运维班组查询
     */
    List<TrArea> findByMaintenanceTeam(String maintenanceTeam);
}
