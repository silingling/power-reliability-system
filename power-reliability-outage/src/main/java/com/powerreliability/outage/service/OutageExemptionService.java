package com.powerreliability.outage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.outage.entity.OutageExemption;

public interface OutageExemptionService extends IService<OutageExemption> {

    OutageExemption autoVerdict(Long eventId);
}
