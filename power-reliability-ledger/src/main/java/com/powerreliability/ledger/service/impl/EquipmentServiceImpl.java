package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Equipment;
import com.powerreliability.ledger.mapper.EquipmentMapper;
import com.powerreliability.ledger.service.IEquipmentService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements IEquipmentService {
}
