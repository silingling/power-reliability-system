package com.powerreliability.governance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.governance.entity.GovernanceEffect;
import com.powerreliability.governance.mapper.GovernanceEffectMapper;
import com.powerreliability.governance.service.GovernanceEffectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 治理成效跟踪服务实现
 */
@Slf4j
@Service
public class GovernanceEffectServiceImpl
        extends ServiceImpl<GovernanceEffectMapper, GovernanceEffect>
        implements GovernanceEffectService {
}
