package com.powerreliability.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.system.entity.SysNotification;
import com.powerreliability.system.mapper.SysNotificationMapper;
import com.powerreliability.system.service.ISysNotificationService;
import org.springframework.stereotype.Service;

@Service
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification> implements ISysNotificationService {
}
