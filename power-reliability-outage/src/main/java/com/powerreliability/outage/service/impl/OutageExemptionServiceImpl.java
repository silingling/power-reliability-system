package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.entity.OutageExemption;
import com.powerreliability.outage.mapper.OutageExemptionMapper;
import com.powerreliability.outage.service.OutageEventService;
import com.powerreliability.outage.service.OutageExemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OutageExemptionServiceImpl extends ServiceImpl<OutageExemptionMapper, OutageExemption> implements OutageExemptionService {

    @Autowired
    private OutageEventService outageEventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutageExemption autoVerdict(Long eventId) {
        OutageEvent event = outageEventService.getById(eventId);
        if (event == null) {
            throw new RuntimeException("停电事件不存在");
        }

        // Check if exemption record already exists
        LambdaQueryWrapper<OutageExemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutageExemption::getEventId, eventId);
        OutageExemption existing = getOne(wrapper);
        if (existing != null) {
            return existing;
        }

        OutageExemption exemption = new OutageExemption();
        exemption.setEventId(eventId);
        exemption.setEventNo(event.getEventNo());
        exemption.setVerdictTime(LocalDateTime.now());

        Integer outageType = event.getOutageType();
        boolean exempted = false;

        // Auto-vote based on outage type
        // 4-外力破坏, 5-极端天气 = force majeure/external damage (exempt)
        // 6-用户侧故障 = user side fault (exempt)
        // 1-计划停电, 2-临时停电, 3-故障停电 = not exempt
        if (outageType != null) {
            switch (outageType) {
                case 4: // 外力破坏
                    exemption.setExemptType(2); // 外力破坏豁免
                    exemption.setExemptReason("自动判定：外力破坏导致的停电");
                    exempted = true;
                    break;
                case 5: // 极端天气
                    exemption.setExemptType(1); // 不可抗力豁免
                    exemption.setExemptReason("自动判定：极端天气导致的停电");
                    exempted = true;
                    break;
                case 6: // 用户侧故障
                    exemption.setExemptType(3); // 用户产权故障豁免
                    exemption.setExemptReason("自动判定：用户侧故障导致的停电");
                    exempted = true;
                    break;
                default:
                    exemption.setExemptType(4); // 其他
                    exemption.setExemptReason("自动判定：不符合豁免条件");
                    break;
            }
        }

        if (exempted) {
            exemption.setVerdictStatus(1); // 已豁免
            event.setIsExempt(1);
            event.setExemptType(exemption.getExemptType());
        } else {
            exemption.setVerdictStatus(2); // 不豁免
            event.setIsExempt(0);
        }

        save(exemption);

        // Update the outage event
        event.setExemptBasis(exemption.getExemptReason());
        outageEventService.updateById(event);

        return exemption;
    }
}
