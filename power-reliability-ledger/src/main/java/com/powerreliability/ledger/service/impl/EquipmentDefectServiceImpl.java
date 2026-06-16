package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.EquipmentDefect;
import com.powerreliability.ledger.mapper.EquipmentDefectMapper;
import com.powerreliability.ledger.service.IEquipmentDefectService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentDefectServiceImpl extends ServiceImpl<EquipmentDefectMapper, EquipmentDefect> implements IEquipmentDefectService {
}
