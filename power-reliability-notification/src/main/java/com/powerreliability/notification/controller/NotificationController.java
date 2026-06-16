package com.powerreliability.notification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.notification.entity.Notification;
import com.powerreliability.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@Tag(name = "消息通知管理")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/list")
    @Operation(summary = "分页查询用户通知")
    public Result<PageResult<Notification>> list(@RequestBody Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 20;
        Long userId = params.get("userId") != null ? Long.parseLong(params.get("userId").toString()) : null;
        String type = params.get("type") != null ? params.get("type").toString() : null;
        Integer isRead = params.get("isRead") != null ? Integer.parseInt(params.get("isRead").toString()) : null;

        Page<Notification> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> result = notificationService.page(pageParam, wrapper);

        PageResult<Notification> pageResult = PageResult.of(
                result.getRecords(), result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
        return Result.ok(pageResult);
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.ok();
    }

    @PostMapping("/batch-read")
    @Operation(summary = "批量已读")
    public Result<Void> batchRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        notificationService.batchMarkAsRead(ids);
        return Result.ok();
    }
}
