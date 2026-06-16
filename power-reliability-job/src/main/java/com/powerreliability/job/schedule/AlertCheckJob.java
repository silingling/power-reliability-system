package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.index.entity.IndexAlert;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.IndexAlertMapper;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.notification.entity.Notification;
import com.powerreliability.notification.mapper.NotificationMapper;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定期检查阈值触发预警
 * 1. 台区停电频次检查（年5次/60天3次）— 按area_id逐个判断
 * 2. SAIDI/SAIFI 阈值检查
 * 3. 生成预警记录 + 推送通知
 */
@Slf4j
@Component
public class AlertCheckJob {

    @Autowired
    private OutageEventMapper outageEventMapper;

    @Autowired
    private ReliabilityIndexMapper reliabilityIndexMapper;

    @Autowired
    private IndexAlertMapper indexAlertMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    private static final double SAIDI_THRESHOLD = 60.0;
    private static final double SAIFI_THRESHOLD = 0.5;

    @Scheduled(cron = "0 */30 * * * ?")
    public void execute() {
        log.info("[AlertCheckJob] 开始阈值预警检查，时间: {}", LocalDateTime.now());

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yearAgo = now.minusYears(1);
            LocalDateTime sixtyDaysAgo = now.minusDays(60);

            // ===== 1. 按台区检查「5次/年、60天/3次」合规阈值 =====
            // 查询近一年所有已闭环非豁免事件
            LambdaQueryWrapper<OutageEvent> wrapperYear = new LambdaQueryWrapper<>();
            wrapperYear.ge(OutageEvent::getOutageStartTime, yearAgo)
                    .eq(OutageEvent::getIsClosed, 1);
            List<OutageEvent> closedEvents = outageEventMapper.selectList(wrapperYear);

            // 按area_id分组统计频次
            Map<Long, Long> yearCount = closedEvents.stream()
                    .filter(e -> e.getAreaId() != null)
                    .collect(Collectors.groupingBy(OutageEvent::getAreaId, Collectors.counting()));

            for (Map.Entry<Long, Long> entry : yearCount.entrySet()) {
                Long areaId = entry.getKey();
                Long count = entry.getValue();

                if (count > 5) {
                    String areaName = closedEvents.stream()
                            .filter(e -> areaId.equals(e.getAreaId()))
                            .map(OutageEvent::getAreaName)
                            .filter(n -> n != null && !n.isEmpty())
                            .findFirst().orElse("台区" + areaId);
                    createAlert("停电频次(年)", "高", "停电频次",
                            count.doubleValue(), 5.0,
                            "台区[" + areaName + "]年累计停电" + count + "次，超阈值5次",
                            areaId.toString());
                }

                // 60天内频次
                long count60 = closedEvents.stream()
                        .filter(e -> areaId.equals(e.getAreaId()))
                        .filter(e -> e.getOutageStartTime() != null && e.getOutageStartTime().isAfter(sixtyDaysAgo))
                        .count();
                if (count60 > 3) {
                    String areaName = closedEvents.stream()
                            .filter(e -> areaId.equals(e.getAreaId()))
                            .map(OutageEvent::getAreaName)
                            .filter(n -> n != null && !n.isEmpty())
                            .findFirst().orElse("台区" + areaId);
                    createAlert("停电频次(60天)", "高", "停电频次",
                            (double) count60, 3.0,
                            "台区[" + areaName + "]60天内停电" + count60 + "次，超阈值3次",
                            areaId.toString());
                }
            }

            // ===== 2. 检查最新 SAIDI/SAIFI 是否超阈值 =====
            LambdaQueryWrapper<ReliabilityIndex> indexWrapper = new LambdaQueryWrapper<>();
            indexWrapper.orderByDesc(ReliabilityIndex::getStatDate)
                    .last("LIMIT 1");
            ReliabilityIndex latest = reliabilityIndexMapper.selectOne(indexWrapper);

            if (latest != null) {
                if (latest.getSaidi() != null && latest.getSaidi() > SAIDI_THRESHOLD) {
                    createAlert("SAIDI", "中", "SAIDI指标",
                            latest.getSaidi(), SAIDI_THRESHOLD,
                            "SAIDI=" + latest.getSaidi() + "分钟，超过阈值" + SAIDI_THRESHOLD + "分钟", "0");
                }
                if (latest.getSaifi() != null && latest.getSaifi() > SAIFI_THRESHOLD) {
                    createAlert("SAIFI", "中", "SAIFI指标",
                            latest.getSaifi(), SAIFI_THRESHOLD,
                            "SAIFI=" + latest.getSaifi() + "次/户，超过阈值" + SAIFI_THRESHOLD + "次/户", "0");
                }
            }

            log.info("[AlertCheckJob] 阈值预警检查完成");
        } catch (Exception e) {
            log.error("[AlertCheckJob] 阈值检查异常", e);
        }
    }

    private void createAlert(String indexName, String level, String alertType,
                              double value, double threshold, String msg, String targetId) {
        try {
            LambdaQueryWrapper<IndexAlert> dupCheck = new LambdaQueryWrapper<>();
            dupCheck.eq(IndexAlert::getIndexName, indexName)
                    .eq(IndexAlert::getStatus, 0)
                    .ge(IndexAlert::getCreateTime, LocalDateTime.now().minusHours(1));
            if (indexAlertMapper.selectCount(dupCheck) > 0) {
                return;
            }

            IndexAlert alert = new IndexAlert();
            alert.setIndexId(0L);
            alert.setStatType(0L);
            alert.setTargetId(Long.valueOf(targetId));
            alert.setAlertType(alertType);
            alert.setAlertLevel(level);
            alert.setIndexName(indexName);
            alert.setIndexValue(value);
            alert.setThresholdValue(threshold);
            alert.setDeviationRate(threshold > 0 ? (value - threshold) / threshold * 100 : 0);
            alert.setAlertTime(LocalDateTime.now());
            alert.setStatus(0);
            alert.setCreateTime(LocalDateTime.now());
            indexAlertMapper.insert(alert);

            Notification notif = new Notification();
            notif.setTitle("[" + level + "] " + indexName + "预警");
            notif.setContent(msg);
            notif.setUserId(0L);
            notif.setType("alert");
            notif.setIsRead(0);
            notif.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notif);

            log.warn("[AlertCheckJob] 触发预警: {}", msg);
        } catch (Exception e) {
            log.error("[AlertCheckJob] 生成预警异常", e);
        }
    }
}
