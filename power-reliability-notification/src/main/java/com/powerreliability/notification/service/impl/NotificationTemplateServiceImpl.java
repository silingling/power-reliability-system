package com.powerreliability.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.notification.entity.NotificationTemplate;
import com.powerreliability.notification.mapper.NotificationTemplateMapper;
import com.powerreliability.notification.service.NotificationTemplateService;
import org.springframework.stereotype.Service;

@Service
public class NotificationTemplateServiceImpl
        extends ServiceImpl<NotificationTemplateMapper, NotificationTemplate>
        implements NotificationTemplateService {
}
