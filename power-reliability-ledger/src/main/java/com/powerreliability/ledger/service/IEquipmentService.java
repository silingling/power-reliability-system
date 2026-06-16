package com.powerreliability.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.ledger.entity.Equipment;

import java.util.List;

public interface IEquipmentService extends IService<Equipment> {

    /**
     * 根据设备状态查询
     */
    List<Equipment> findByStatus(Integer equipmentStatus);

    /**
     * 批量更新设备状态
     */
    int batchUpdateStatus(List<Long> ids, Integer equipmentStatus);

    /**
     * 根据台区编码查询设备
     */
    List<Equipment> findByAreaCode(String areaCode);

    /**
     * 根据线路编码查询设备
     */
    List<Equipment> findByLineCode(String lineCode);
}
