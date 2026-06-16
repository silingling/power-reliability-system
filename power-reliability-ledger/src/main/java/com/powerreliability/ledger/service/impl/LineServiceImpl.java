package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Line;
import com.powerreliability.ledger.mapper.LineMapper;
import com.powerreliability.ledger.service.ILineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LineServiceImpl extends ServiceImpl<LineMapper, Line> implements ILineService {

    @Override
    public List<Line> findByStatus(Integer status) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getStatus, status);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        LambdaUpdateWrapper<Line> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Line::getId, ids)
               .set(Line::getStatus, status);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public List<Line> findByAreaCode(String areaCode) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getAreaCode, areaCode);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Line> findByLineType(Integer lineType) {
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Line::getLineType, lineType);
        return baseMapper.selectList(wrapper);
    }
}
