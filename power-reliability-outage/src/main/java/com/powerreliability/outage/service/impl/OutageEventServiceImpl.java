package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.OutageArchive;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageArchiveMapper;
import com.powerreliability.outage.mapper.OutageEventMapper;
import com.powerreliability.outage.service.OutageEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutageEventServiceImpl
        extends ServiceImpl<OutageEventMapper, OutageEvent>
        implements OutageEventService {

    private final OutageArchiveMapper outageArchiveMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long eventId) {
        OutageEvent event = getById(eventId);
        if (event == null) {
            log.warn("停电事件不存在, eventId={}", eventId);
            return;
        }

        // 构建归档内容
        String archiveContent = String.format(
                "{\"eventNo\":\"%s\",\"outageType\":%d,\"startTime\":\"%s\",\"endTime\":\"%s\",\"duration\":%d,\"affectedCount\":%d,\"faultType\":\"%s\",\"faultReason\":\"%s\",\"isClosed\":%d}",
                event.getEventNo(), event.getOutageType(),
                event.getOutageStartTime(), event.getOutageEndTime(),
                event.getOutageDuration(), event.getAffectedConsumerCount(),
                event.getFaultType(), event.getFaultReason(),
                event.getIsClosed());

        OutageArchive archive = new OutageArchive();
        archive.setEventId(event.getId());
        archive.setEventNo(event.getEventNo());
        archive.setArchiveType(1);
        archive.setArchiveContent(archiveContent);
        archive.setArchiveSize(archiveContent.length());
        archive.setArchiveTime(LocalDateTime.now());

        outageArchiveMapper.insert(archive);
        log.info("停电事件归档完成, eventId={}, archiveId={}", eventId, archive.getId());
    }

    @Override
    public Map<String, Object> calculateReliabilityImpact(Long eventId) {
        OutageEvent event = getById(eventId);
        if (event == null) {
            log.warn("停电事件不存在, eventId={}", eventId);
            return Map.of();
        }

        // 豁免事件不纳入指标计算
        if (event.getIsExempt() != null && event.getIsExempt() == 1) {
            log.info("豁免事件不计入可靠性指标, eventId={}", eventId);
            return new HashMap<>();
        }

        Integer duration = event.getOutageDuration();        // 停电时长（分钟）
        Integer affectedCount = event.getAffectedConsumerCount(); // 影响用户数

        if (duration == null || duration == 0 || affectedCount == null || affectedCount == 0) {
            return new HashMap<>();
        }

        // 影响分钟数（用户·分钟）
        double affectedMinutes = (double) duration * affectedCount;

        // 假设总用户数为 affectedCount 的 100 倍用于计算 SAIDI/SAIFI（实际应从台区档案获取）
        double totalConsumers = affectedCount * 100.0;

        // SAIDI = Σ(停电时长 × 影响用户数) / 总用户数
        double saidi = affectedMinutes / totalConsumers;

        // SAIFI = 停电次数 / 总用户数（单次事件为 1 / 总用户数）
        double saifi = 1.0 / totalConsumers;

        // CAIDI = SAIDI / SAIFI
        double caidi = saidi / saifi;

        Map<String, Object> impact = new HashMap<>();
        impact.put("eventId", eventId);
        impact.put("saidi", saidi);
        impact.put("saifi", saifi);
        impact.put("caidi", caidi);
        impact.put("affectedMinutes", affectedMinutes);
        impact.put("affectedConsumerCount", affectedCount);
        impact.put("outageDuration", duration);

        log.info("停电事件影响计算完成, eventId={}, SAIDI={}, SAIFI={}", eventId, saidi, saifi);
        return impact;
    }
}
