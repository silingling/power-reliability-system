package com.powerreliability.notification.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.notification.entity.Notification;
import com.powerreliability.notification.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知发送消费 Job
 *
 * 每 5 分钟扫描所有未发送的 Notification 记录，模拟发送后标记为已读。
 * 实际生产环境可扩展为：
 * - 短信网关（阿里云/腾讯云 SMS）
 * - 邮件 SMTP
 * - 飞书消息（Lark Open API）
 * - Webhook 回调
 */
@Slf4j
@Component
public class NotificationSendJob {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 每 5 分钟处理一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        log.debug("[NotificationSendJob] 开始处理待发送通知");

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 0);
        wrapper.orderByAsc(Notification::getCreateTime);
        wrapper.last("LIMIT 50");

        List<Notification> pending = notificationMapper.selectList(wrapper);

        if (pending.isEmpty()) {
            return;
        }

        int sent = 0;
        for (Notification notif : pending) {
            try {
                // ===== 实际发送逻辑（根据 type 选择渠道） =====
                boolean deliverSuccess = deliver(notif);

                if (deliverSuccess) {
                    // 标记为已读
                    notif.setIsRead(1);
                    notif.setUpdateTime(LocalDateTime.now());
                    notificationMapper.updateById(notif);
                    sent++;
                    log.info("[NotificationSendJob] 通知已发送: id={}, title={}, type={}",
                            notif.getId(), notif.getTitle(), notif.getType());
                } else {
                    log.warn("[NotificationSendJob] 通知发送失败: id={}, title={}", notif.getId(), notif.getTitle());
                }
            } catch (Exception e) {
                log.error("[NotificationSendJob] 发送通知异常: id={}", notif.getId(), e);
            }
        }

        if (sent > 0) {
            log.info("[NotificationSendJob] 本轮共发送 {} 条通知", sent);
        }
    }

    /**
     * 实际投递通知
     * 目前为演示实现，根据不同 type 写入日志
     * 未来可替换为：
     * - type=alert  → 飞书/短信紧急通知
     * - type=task   → 任务系统消息
     * - type=system → 站内信
     * - type=approval → 审批通知
     */
    private boolean deliver(Notification notif) {
        String channel;
        switch (notif.getType() != null ? notif.getType() : "system") {
            case "alert":
                channel = "【紧急通道】短信/飞书";
                break;
            case "task":
                channel = "任务中心";
                break;
            case "approval":
                channel = "审批系统";
                break;
            default:
                channel = "站内信";
        }

        log.info("[NotificationSendJob] 投递通知: channel={}, title={}, content={}, userId={}",
                channel, notif.getTitle(), notif.getContent(), notif.getUserId());

        // 实际发送调用（示例：飞书机器人消息）
        // if ("alert".equals(notif.getType()) && notif.getUserId() != null) {
        //     feishuClient.sendMessage(notif.getUserId(), notif.getTitle() + "\n" + notif.getContent());
        // }

        return true;
    }
}
