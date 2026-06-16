package com.powerreliability.job.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.governance.entity.GovernanceOrder;
import com.powerreliability.governance.mapper.GovernanceOrderMapper;
import com.powerreliability.notification.entity.Notification;
import com.powerreliability.notification.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 治理工单超时提醒
 * - 检查待处理/治理中工单的截止日期
 * - 24小时内截止的 → 预警提醒
 * - 已超期的 → 标记超期 + 通知升级
 */
@Slf4j
@Component
public class GovernanceReviewJob {

    @Autowired
    private GovernanceOrderMapper governanceOrderMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 每 2 小时检查一次
     */
    @Scheduled(cron = "0 0 */2 * * ?")
    public void execute() {
        log.info("[GovernanceReviewJob] 开始治理工单超时检查，时间: {}", LocalDateTime.now());

        try {
            LocalDate now = LocalDate.now();
            LocalDate warningThreshold = now.plusDays(1); // 24小时内到期

            // 1. 查询所有待处理/治理中的治理工单 (status=0 or 1)
            LambdaQueryWrapper<GovernanceOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(GovernanceOrder::getStatus, 0, 1);
            List<GovernanceOrder> orders = governanceOrderMapper.selectList(wrapper);

            for (GovernanceOrder order : orders) {
                if (order.getDeadline() == null) continue;

                long daysUntilDeadline = ChronoUnit.DAYS.between(now, order.getDeadline());
                long daysOverdue = ChronoUnit.DAYS.between(order.getDeadline(), now);

                if (daysOverdue > 0) {
                    // 已超期 → 标记 + 发送超期通知
                    order.setStatus(1); // 保持治理中
                    governanceOrderMapper.updateById(order);

                    sendNotification(order, "超期",
                            "治理工单「" + order.getTitle() + "」已超期 " + daysOverdue + " 天，请立即处理！");

                    log.warn("[GovernanceReviewJob] 工单超期: {} (超期{})", order.getOrderNo(), daysOverdue);

                } else if (daysUntilDeadline <= 1 && daysUntilDeadline >= 0) {
                    // 24小时内截止 → 预警提醒
                    sendNotification(order, "即将到期",
                            "治理工单「" + order.getTitle() + "」将于 " + daysUntilDeadline + " 天内截止，请尽快处理！");

                    log.info("[GovernanceReviewJob] 工单即将到期: {} (剩余{}天)", order.getOrderNo(), daysUntilDeadline);

                } else if (daysOverdue > 7) {
                    // 严重超期（>7天）→ 升级通知
                    sendNotification(order, "严重超期",
                            "治理工单「" + order.getTitle() + "」已严重超期 " + daysOverdue + " 天，需上级督办！");
                }
            }

            log.info("[GovernanceReviewJob] 治理工单超时检查完成，共检查 {} 条工单", orders.size());
        } catch (Exception e) {
            log.error("[GovernanceReviewJob] 工单超时检查异常", e);
        }
    }

    private void sendNotification(GovernanceOrder order, String type, String content) {
        try {
            Notification notif = new Notification();
            notif.setTitle("治理工单" + type + "提醒");
            notif.setContent(content);
            notif.setUserId(0L);
            notif.setType("task");
            notif.setIsRead(0);
            notif.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notif);
        } catch (Exception e) {
            log.error("[GovernanceReviewJob] 发送通知异常", e);
        }
    }
}
