package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Equipment;
import com.powerreliability.ledger.mapper.EquipmentMapper;
import com.powerreliability.ledger.service.IEquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements IEquipmentService {

    @Override
    public List<Equipment> findByStatus(Integer equipmentStatus) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Equipment::getEquipmentStatus, equipmentStatus);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer equipmentStatus) {
        LambdaUpdateWrapper<Equipment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Equipment::getId, ids)
               .set(Equipment::getEquipmentStatus, equipmentStatus);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public List<Equipment> findByAreaCode(String areaCode) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Equipment::getAreaCode, areaCode);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Equipment> findByLineCode(String lineCode) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Equipment::getLineCode, lineCode);
        return baseMapper.selectList(wrapper);
    }
}
