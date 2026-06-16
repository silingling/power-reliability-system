package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.mapper.TrAreaMapper;
import com.powerreliability.ledger.service.ITrAreaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrAreaServiceImpl extends ServiceImpl<TrAreaMapper, TrArea> implements ITrAreaService {

    @Override
    public List<TrArea> findByStatus(Integer areaStatus) {
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrArea::getAreaStatus, areaStatus);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer areaStatus) {
        LambdaUpdateWrapper<TrArea> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(TrArea::getId, ids)
               .set(TrArea::getAreaStatus, areaStatus);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public List<TrArea> findByPowerSupplyStation(String powerSupplyStation) {
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrArea::getPowerSupplyStation, powerSupplyStation);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<TrArea> findByMaintenanceTeam(String maintenanceTeam) {
        LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrArea::getMaintenanceTeam, maintenanceTeam);
        return baseMapper.selectList(wrapper);
    }
}
