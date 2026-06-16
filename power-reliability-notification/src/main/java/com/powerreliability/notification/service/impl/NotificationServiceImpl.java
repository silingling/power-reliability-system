package com.powerreliability.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.notification.entity.Notification;
import com.powerreliability.notification.mapper.NotificationMapper;
import com.powerreliability.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        boolean updated = update(wrapper);
        if (updated) {
            log.info("通知已标记已读, id={}", id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Notification::getId, ids)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        int count = baseMapper.update(null, wrapper);
        log.info("批量标记已读完成, 数量={}", count);
    }
}
