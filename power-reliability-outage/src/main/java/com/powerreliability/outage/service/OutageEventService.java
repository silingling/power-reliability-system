package com.powerreliability.outage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.outage.entity.OutageEvent;

public interface OutageEventService extends IService<OutageEvent> {

    /**
     * 将超期停电事件归档到 OutageArchive 表
     *
     * @param eventId 停电事件ID
     */
    void archive(Long eventId);

    /**
     * 计算单个停电事件对 SAIDI/SAIFI 的影响值
     *
     * @param eventId 停电事件ID
     * @return 影响数据Map（saidi, saifi, caidi, affectedMinutes）
     */
    java.util.Map<String, Object> calculateReliabilityImpact(Long eventId);
}
}
