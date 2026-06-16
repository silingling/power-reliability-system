package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.mapper.ConsumerMapper;
import com.powerreliability.ledger.service.IConsumerService;
import org.springframework.stereotype.Service;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements IConsumerService {
}
