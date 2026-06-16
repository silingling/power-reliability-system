package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.FaultOutage;
import com.powerreliability.outage.mapper.FaultOutageMapper;
import com.powerreliability.outage.service.FaultOutageService;
import org.springframework.stereotype.Service;

@Service
public class FaultOutageServiceImpl extends ServiceImpl<FaultOutageMapper, FaultOutage> implements FaultOutageService {
}
