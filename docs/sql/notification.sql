-- ===================================================
-- 消息通知模块 - 表结构
-- ===================================================

USE power_reliability;

-- 1. 消息通知表
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(50) NOT NULL COMMENT '通知类型: alert-预警, task-任务, system-系统, approval-审批',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    biz_id VARCHAR(100) COMMENT '关联业务ID',
    biz_type VARCHAR(50) COMMENT '关联业务类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_type (type),
    KEY idx_is_read (is_read),
    KEY idx_create_time (create_time),
    KEY idx_user_type_read (user_id, type, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 2. 消息模板表
CREATE TABLE notification_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    title VARCHAR(200) COMMENT '模板标题',
    content TEXT COMMENT '模板内容（支持占位符 {0}, {1} 等）',
    type VARCHAR(50) NOT NULL COMMENT '通知类型: alert-预警, task-任务, system-系统, approval-审批',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_type (type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板表';
