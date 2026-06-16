package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.outage.entity.OutageArchive;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.entity.OutageExemption;
import com.powerreliability.outage.entity.PlannedOutage;
import com.powerreliability.outage.entity.FaultOutage;
import com.powerreliability.outage.mapper.OutageArchiveMapper;
import com.powerreliability.outage.mapper.OutageEventMapper;
import com.powerreliability.outage.mapper.OutageExemptionMapper;
import com.powerreliability.outage.mapper.PlannedOutageMapper;
import com.powerreliability.outage.mapper.FaultOutageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超期停电事件自动归档
 * 查询所有已闭环超过 30 天且未归档的停电事件，构建归档记录
 */
@Slf4j
@Component
public class OutageArchiveJob {

    @Autowired
    private OutageEventMapper outageEventMapper;

    @Autowired
    private PlannedOutageMapper plannedOutageMapper;

    @Autowired
    private FaultOutageMapper faultOutageMapper;

    @Autowired
    private OutageExemptionMapper outageExemptionMapper;

    @Autowired
    private OutageArchiveMapper outageArchiveMapper;

    /**
     * 每天凌晨 3:00 执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        log.info("[OutageArchiveJob] 开始超期停电事件自动归档，时间: {}", LocalDateTime.now());

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);

            // 1. 查询所有已闭环超过 30 天且未归档的停电事件
            LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OutageEvent::getIsClosed, 1)  // 已闭环
                    .eq(OutageEvent::getIsArchived, 0)   // 未归档
                    .le(OutageEvent::getOutageEndTime, threshold);
            List<OutageEvent> events = outageEventMapper.selectList(wrapper);

            if (events.isEmpty()) {
                log.info("[OutageArchiveJob] 无需要归档的停电事件");
                return;
            }

            int archived = 0;
            for (OutageEvent event : events) {
                try {
                    // 2. 构建归档内容（全流程信息）
                    StringBuilder content = new StringBuilder();
                    content.append("停电事件编号: ").append(event.getEventNo()).append("\n");
                    content.append("停电时间: ").append(event.getOutageStartTime())
                            .append(" ~ ").append(event.getOutageEndTime()).append("\n");
                    content.append("停电类型: ").append(event.getOutageType()).append("\n");
                    content.append("影响区域: ").append(event.getAreaName()).append("\n");
                    content.append("影响用户数: ").append(event.getAffectedConsumerCount()).append("\n");
                    content.append("停电原因: ").append(event.getFaultReason()).append("\n");

                    // 查询关联的计划/故障/豁免信息
                    LambdaQueryWrapper<PlannedOutage> plannedWrapper = new LambdaQueryWrapper<>();
                    plannedWrapper.eq(PlannedOutage::getEventId, event.getId());
                    PlannedOutage planned = plannedOutageMapper.selectOne(plannedWrapper);
                    if (planned != null) {
                        content.append("计划执行状态: ").append(planned.getExecutionStatus()).append("\n");
                        content.append("审批记录: ").append(planned.getApprovalRecord()).append("\n");
                    }

                    LambdaQueryWrapper<FaultOutage> faultWrapper = new LambdaQueryWrapper<>();
                    faultWrapper.eq(FaultOutage::getEventId, event.getId());
                    FaultOutage fault = faultOutageMapper.selectOne(faultWrapper);
                    if (fault != null) {
                        content.append("故障来源: ").append(fault.getFaultSource()).append("\n");
                        content.append("故障类型: ").append(fault.getFaultType()).append("\n");
                        content.append("恢复方式: ").append(fault.getRecoveryMethod()).append("\n");
                    }

                    LambdaQueryWrapper<OutageExemption> exemptWrapper = new LambdaQueryWrapper<>();
                    exemptWrapper.eq(OutageExemption::getEventId, event.getId());
                    OutageExemption exemption = outageExemptionMapper.selectOne(exemptWrapper);
                    if (exemption != null) {
                        content.append("豁免类型: ").append(exemption.getExemptType()).append("\n");
                        content.append("豁免原因: ").append(exemption.getExemptReason()).append("\n");
                    }

                    // 3. 写入 outage_archive 表
                    OutageArchive archive = new OutageArchive();
                    archive.setEventId(event.getId());
                    archive.setEventNo(event.getEventNo());
                    archive.setArchiveType(0); // 自动归档
                    archive.setArchiveContent(content.toString());
                    archive.setArchiveTime(LocalDateTime.now());
                    archive.setCreateTime(LocalDateTime.now());
                    outageArchiveMapper.insert(archive);

                    // 4. 标记原事件为已归档
                    event.setIsArchived(1);
                    event.setUpdateTime(LocalDateTime.now());
                    outageEventMapper.updateById(event);

                    archived++;
                    log.info("[OutageArchiveJob] 已归档事件: {} (id={})", event.getEventNo(), event.getId());
                } catch (Exception e) {
                    log.error("[OutageArchiveJob] 归档事件失败: id={}", event.getId(), e);
                }
            }

            log.info("[OutageArchiveJob] 超期停电事件归档完成: 共处理 {} 条，成功 {} 条", events.size(), archived);
        } catch (Exception e) {
            log.error("[OutageArchiveJob] 自动归档异常", e);
        }
    }
}
