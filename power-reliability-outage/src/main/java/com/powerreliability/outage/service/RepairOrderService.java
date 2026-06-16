package com.powerreliability.outage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.outage.entity.RepairOrder;

public interface RepairOrderService extends IService<RepairOrder> {

    void startRepair(Long id);

    void completeRepair(Long id);

    void verifyRepair(Long id, Integer result);
}
