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

@RestController
@RequestMapping("/api/notification")
@Tag(name = "消息通知管理")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "分页查询用户通知")
    public Result<PageResult<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer isRead) {
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
    public Result<Void> batchRead(@RequestBody BatchReadRequest request) {
        notificationService.batchMarkAsRead(request.getIds());
        return Result.ok();
    }

    @GetMapping("/count-unread")
    @Operation(summary = "获取未读消息数量")
    public Result<Long> countUnread(@RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 0);
        if (userId != null) {
            wrapper.eq(Notification::getUserId, userId);
        }
        long count = notificationService.count(wrapper);
        return Result.ok(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知消息")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return Result.ok();
    }

    @lombok.Data
    public static class BatchReadRequest {
        private List<Long> ids;
    }
}
