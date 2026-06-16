package com.powerreliability.governance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.governance.entity.GovernanceEffect;

/**
 * 治理成效跟踪服务
 */
public interface GovernanceEffectService extends IService<GovernanceEffect> {

    /**
     * 治理前后停电次数/时长对比分析
     *
     * @param ledgerId 频繁停电台账ID
     * @return 对比数据Map
     */
    java.util.Map<String, Object> compareBeforeAfter(Long ledgerId);

    /**
     * 治理有效率统计
     *
     * @param startTime 统计起始时间
     * @param endTime   统计结束时间
     * @return 有效率统计Map
     */
    java.util.Map<String, Object> getEffectivenessRate(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
}
