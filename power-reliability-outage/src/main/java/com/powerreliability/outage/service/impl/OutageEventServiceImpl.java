package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import com.powerreliability.outage.service.OutageEventService;
import org.springframework.stereotype.Service;

@Service
public class OutageEventServiceImpl extends ServiceImpl<OutageEventMapper, OutageEvent> implements OutageEventService {
}
