package com.powerreliability.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.notification.entity.Notification;

public interface NotificationService extends IService<Notification> {

    /**
     * 标记通知已读
     *
     * @param id 通知ID
     */
    void markAsRead(Long id);

    /**
     * 批量标记已读
     *
     * @param ids 通知ID列表
     */
    void batchMarkAsRead(java.util.List<Long> ids);
}
