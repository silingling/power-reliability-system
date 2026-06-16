package com.powerreliability.governance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.governance.entity.OutageCauseAnalysis;
import com.powerreliability.governance.mapper.OutageCauseAnalysisMapper;
import com.powerreliability.governance.service.OutageCauseAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 停电归因分析服务实现
 */
@Slf4j
@Service
public class OutageCauseAnalysisServiceImpl
        extends ServiceImpl<OutageCauseAnalysisMapper, OutageCauseAnalysis>
        implements OutageCauseAnalysisService {
}
