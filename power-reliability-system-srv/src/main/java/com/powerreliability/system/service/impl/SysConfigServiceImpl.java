package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysConfig;
import com.powerreliability.system.mapper.SysConfigMapper;
import com.powerreliability.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
}
