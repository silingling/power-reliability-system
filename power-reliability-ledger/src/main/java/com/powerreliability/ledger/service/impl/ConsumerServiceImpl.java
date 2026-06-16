package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.mapper.ConsumerMapper;
import com.powerreliability.ledger.service.IConsumerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements IConsumerService {

    @Override
    public List<Consumer> findByStatus(Integer status) {
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consumer::getStatus, status);
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ids, Integer status) {
        LambdaUpdateWrapper<Consumer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Consumer::getId, ids)
               .set(Consumer::getStatus, status);
        return baseMapper.update(null, wrapper);
    }

    @Override
    public List<Consumer> findByAreaCode(String areaCode) {
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consumer::getAreaCode, areaCode);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Consumer> findHighOutageUsers(Integer threshold) {
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Consumer::getOutageCountYear, threshold);
        return baseMapper.selectList(wrapper);
    }
}
