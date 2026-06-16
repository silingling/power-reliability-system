package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.PlannedOutage;
import com.powerreliability.outage.mapper.PlannedOutageMapper;
import com.powerreliability.outage.service.PlannedOutageService;
import org.springframework.stereotype.Service;

@Service
public class PlannedOutageServiceImpl extends ServiceImpl<PlannedOutageMapper, PlannedOutage> implements PlannedOutageService {
}
