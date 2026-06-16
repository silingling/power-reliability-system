-- ===================================================
-- 定时任务模块 - 任务配置表
-- ===================================================

USE power_reliability;

-- 1. 定时任务配置表
CREATE TABLE job_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(100) DEFAULT 'DEFAULT' COMMENT '任务组',
    job_class VARCHAR(500) NOT NULL COMMENT '任务实现类全限定名',
    cron_expression VARCHAR(200) NOT NULL COMMENT 'Cron表达式',
    param_json TEXT COMMENT '任务参数(JSON格式)',
    description VARCHAR(500) COMMENT '任务描述',
    job_status TINYINT DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    misfire_instruction TINYINT DEFAULT 1 COMMENT '错失执行策略: 1-忽略, 2-立即补偿, 3-不触发',
    concurrent TINYINT DEFAULT 0 COMMENT '是否允许并发: 0-不允许, 1-允许',
    last_execute_time DATETIME COMMENT '上次执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    execute_count INT DEFAULT 0 COMMENT '累计执行次数',
    last_execute_result TINYINT COMMENT '上次执行结果: 0-失败, 1-成功',
    last_error_msg TEXT COMMENT '上次错误信息',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_name_group (job_name, job_group),
    KEY idx_job_status (job_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务配置表';

-- 2. 任务执行日志表
CREATE TABLE job_execution_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(100) DEFAULT 'DEFAULT' COMMENT '任务组',
    trigger_time DATETIME NOT NULL COMMENT '触发时间',
    start_time DATETIME COMMENT '开始执行时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms INT COMMENT '执行耗时(毫秒)',
    execute_result TINYINT COMMENT '执行结果: 0-失败, 1-成功',
    error_msg TEXT COMMENT '错误信息',
    result_data TEXT COMMENT '执行结果数据(JSON)',
    fire_instance_id VARCHAR(100) COMMENT '触发器实例ID',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_job_id (job_id),
    KEY idx_trigger_time (trigger_time),
    KEY idx_execute_result (execute_result),
    KEY idx_job_time (job_id, trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

-- 3. 初始化默认任务配置
INSERT INTO job_config (job_name, job_group, job_class, cron_expression, description, job_status, misfire_instruction, concurrent) VALUES
('IndexCalculateJob', 'RELIABILITY', 'com.powerreliability.job.schedule.IndexCalculateJob', '0 0 2 * * ?', '每日凌晨自动计算SAIDI/SAIFI等可靠性指标', 1, 1, 0),
('AlertCheckJob', 'RELIABILITY', 'com.powerreliability.job.schedule.AlertCheckJob', '0 */30 * * * ?', '定期检查阈值触发预警', 1, 1, 0),
('OutageArchiveJob', 'RELIABILITY', 'com.powerreliability.job.schedule.OutageArchiveJob', '0 0 3 * * ?', '超期停电事件自动归档', 1, 1, 0),
('GovernanceReviewJob', 'RELIABILITY', 'com.powerreliability.job.schedule.GovernanceReviewJob', '0 0 */2 * * ?', '治理工单超时提醒', 1, 1, 0);
