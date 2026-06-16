package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.RepairOrder;
import com.powerreliability.outage.mapper.RepairOrderMapper;
import com.powerreliability.outage.service.RepairOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long id) {
        RepairOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        order.setOrderStatus(2); // 2-处理中
        order.setArriveTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long id) {
        RepairOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        order.setOrderStatus(3); // 3-已完成
        order.setRepairCompleteTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRepair(Long id, Integer result) {
        RepairOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }
        order.setOrderStatus(4); // 4-已核验
        order.setVerifyResult(result);
        order.setRecoveryVerifyTime(LocalDateTime.now());
        updateById(order);
    }
}
