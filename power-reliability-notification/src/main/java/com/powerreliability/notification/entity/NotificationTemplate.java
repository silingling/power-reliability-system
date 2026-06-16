package com.powerreliability.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息模板
 */
@Data
@TableName("notification_template")
public class NotificationTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板编码 */
    private String templateCode;

    /** 模板名称 */
    private String templateName;

    /** 模板标题 */
    private String title;

    /** 模板内容（支持占位符 {0}, {1} 等） */
    private String content;

    /** 通知类型: alert-预警, task-任务, system-系统, approval-审批 */
    private String type;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
