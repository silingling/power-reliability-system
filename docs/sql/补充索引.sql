-- ===================================================
-- 补充业务查询所需索引
-- ===================================================

USE power_reliability;

-- ==================== 停电事件相关 ====================

-- outage_event 表补充索引
ALTER TABLE outage_event ADD INDEX idx_event_no (event_no) COMMENT '事件编号查询';
ALTER TABLE outage_event ADD INDEX idx_area_name (area_name) COMMENT '台区名称查询';
ALTER TABLE outage_event ADD INDEX idx_fault_reason (fault_reason(100)) COMMENT '故障原因模糊查询';
ALTER TABLE outage_event ADD INDEX idx_affected_count (affected_consumer_count) COMMENT '影响用户数统计';
ALTER TABLE outage_event ADD INDEX idx_outage_duration (outage_duration) COMMENT '停电时长查询';
ALTER TABLE outage_event ADD INDEX idx_close_time (close_time) COMMENT '闭环时间查询';
ALTER TABLE outage_event ADD INDEX idx_is_closed (is_closed) COMMENT '是否闭环筛选';
ALTER TABLE outage_event ADD INDEX idx_composite_query (outage_type, is_exempt, outage_start_time) COMMENT '复合查询：类型+豁免+时间';

-- outage_archive 表补充索引
ALTER TABLE outage_archive ADD INDEX idx_event_no (event_no) COMMENT '归档事件编号查询';
ALTER TABLE outage_archive ADD INDEX idx_archive_type (archive_type) COMMENT '归档类型筛选';

-- planned_outage 表补充索引
ALTER TABLE planned_outage ADD INDEX idx_plan_time (plan_start_time, plan_end_time) COMMENT '计划时间范围查询';
ALTER TABLE planned_outage ADD INDEX idx_execution_status (execution_status) COMMENT '执行状态筛选';
ALTER TABLE planned_outage ADD INDEX idx_is_overtime (is_overtime) COMMENT '超时筛选';

-- fault_outage 表补充索引
ALTER TABLE fault_outage ADD INDEX idx_fault_source (fault_source) COMMENT '故障来源统计';
ALTER TABLE fault_outage ADD INDEX idx_recovery_time (recovery_time) COMMENT '恢复时间查询';
ALTER TABLE fault_outage ADD INDEX idx_fault_type (fault_type(50)) COMMENT '故障类型查询';

-- repair_order 表补充索引
ALTER TABLE repair_order ADD INDEX idx_dispatch_time (dispatch_time) COMMENT '派单时间查询';
ALTER TABLE repair_order ADD INDEX idx_verify_result (verify_result) COMMENT '核验结果统计';
ALTER TABLE repair_order ADD INDEX idx_repair_team (repair_team(100)) COMMENT '抢修班组统计';
ALTER TABLE repair_order ADD INDEX idx_arrive_time (arrive_time) COMMENT '到场时间查询';

-- ==================== 频繁停电治理相关 ====================

-- frequent_outage_ledger 表补充索引
ALTER TABLE frequent_outage_ledger ADD INDEX idx_area_id (area_id) COMMENT '台区ID查询';
ALTER TABLE frequent_outage_ledger ADD INDEX idx_violation_type (violation_type) COMMENT '超标类型筛选';
ALTER TABLE frequent_outage_ledger ADD INDEX idx_is_recurrence (is_recurrence) COMMENT '反弹情况筛选';
ALTER TABLE frequent_outage_ledger ADD INDEX idx_governance_date (governance_start_date, governance_end_date) COMMENT '治理时间范围查询';

-- outage_cause_analysis 表补充索引
ALTER TABLE outage_cause_analysis ADD INDEX idx_analysis_confidence (confidence) COMMENT '置信度排序';
ALTER TABLE outage_cause_analysis ADD INDEX idx_cause_category (cause_category(50)) COMMENT '归因类别筛选';
ALTER TABLE outage_cause_analysis ADD INDEX idx_area_code_cause (area_code, cause_type) COMMENT '区域+原因联合查询';

-- governance_order 表补充索引
ALTER TABLE governance_order ADD INDEX idx_order_no (order_no) COMMENT '工单编号查询';
ALTER TABLE governance_order ADD INDEX idx_responsible_unit (responsible_unit(100)) COMMENT '责任单位查询';
ALTER TABLE governance_order ADD INDEX idx_responsible_person (responsible_person(50)) COMMENT '责任人查询';
ALTER TABLE governance_order ADD INDEX idx_deadline (deadline) COMMENT '截止日期查询';
ALTER TABLE governance_order ADD INDEX idx_urgency_level (urgency_level) COMMENT '紧急程度筛选';
ALTER TABLE governance_order ADD INDEX idx_is_appeal (is_appeal) COMMENT '超期状态查询';

-- governance_effect 表补充索引
ALTER TABLE governance_effect ADD INDEX idx_effect_score (effect_score) COMMENT '成效评分查询';
ALTER TABLE governance_effect ADD INDEX idx_effect_time (effect_time) COMMENT '成效时间查询';
ALTER TABLE governance_effect ADD INDEX idx_create_time (create_time) COMMENT '创建时间查询';

-- ==================== 可靠性指标相关 ====================

-- reliability_index 表补充索引（已有 uk_stat 唯一索引）
ALTER TABLE reliability_index ADD INDEX idx_stat_target (stat_target_id) COMMENT '统计目标查询';
ALTER TABLE reliability_index ADD INDEX idx_stat_end_date (stat_end_date) COMMENT '结束日期查询';
ALTER TABLE reliability_index ADD INDEX idx_stat_period_target (stat_period, stat_target_id, stat_target_name(50)) COMMENT '周期+目标联合查询';

-- index_alert 表补充索引
ALTER TABLE index_alert ADD INDEX idx_alert_type (index_type(50)) COMMENT '指标类型筛选';
ALTER TABLE index_alert ADD INDEX idx_trend (trend_direction) COMMENT '趋势方向筛选';
ALTER TABLE index_alert ADD INDEX idx_handler (handler(50)) COMMENT '处理人查询';
ALTER TABLE index_alert ADD INDEX idx_handled_time (handled_time) COMMENT '处理时间查询';

-- ==================== 隐患预判与预警相关 ====================

-- risk_prediction 表补充索引
ALTER TABLE risk_prediction ADD INDEX idx_risk_score (risk_score) COMMENT '风险评分排序';
ALTER TABLE risk_prediction ADD INDEX idx_risk_type (risk_type) COMMENT '风险类型筛选';
ALTER TABLE risk_prediction ADD INDEX idx_area_id (area_id) COMMENT '台区ID查询';

-- risk_warning_order 表补充索引
ALTER TABLE risk_warning_order ADD INDEX idx_warning_time (warning_time) COMMENT '预警时间查询';
ALTER TABLE risk_warning_order ADD INDEX idx_response_deadline (response_deadline) COMMENT '处置时限查询';
ALTER TABLE risk_warning_order ADD INDEX idx_disposal_person (disposal_person(50)) COMMENT '处置人查询';

-- ==================== 复盘整改相关 ====================

-- review_report 表补充索引
ALTER TABLE review_report ADD INDEX idx_report_status (report_status) COMMENT '报告状态筛选';
ALTER TABLE review_report ADD INDEX idx_report_period_end (report_period_end) COMMENT '期间结束日期查询';

-- rectification_task 表补充索引
ALTER TABLE rectification_task ADD INDEX idx_task_no (task_no) COMMENT '任务编号查询';
ALTER TABLE rectification_task ADD INDEX idx_responsible_dept (responsible_dept(100)) COMMENT '责任部门查询';
ALTER TABLE rectification_task ADD INDEX idx_deadline_date (deadline_date) COMMENT '整改时限查询';
ALTER TABLE rectification_task ADD INDEX idx_is_repeated (is_repeated) COMMENT '重复问题筛选';

-- ==================== 绩效考核相关 ====================

-- performance_assessment 表补充索引
ALTER TABLE performance_assessment ADD INDEX idx_assessment_grade (assessment_grade(10)) COMMENT '考核等级筛选';
ALTER TABLE performance_assessment ADD INDEX idx_total_score (total_score) COMMENT '综合评分排序';

-- ==================== 台账相关 ====================

-- tr_area 表补充索引
ALTER TABLE tr_area ADD INDEX idx_area_status (area_status) COMMENT '台区状态筛选';
ALTER TABLE tr_area ADD INDEX idx_responsible_person (responsible_person(50)) COMMENT '责任人在册查询';
ALTER TABLE tr_area ADD INDEX idx_commissioning_date (commissioning_date) COMMENT '投运日期查询';

-- consumer 表补充索引
ALTER TABLE consumer ADD INDEX idx_consumer_status (status) COMMENT '用户状态筛选';
ALTER TABLE consumer ADD INDEX idx_outage_count (outage_count_year, outage_count_60d) COMMENT '停电次数联合查询';
ALTER TABLE consumer ADD INDEX idx_contact_phone (contact_phone(20)) COMMENT '联系方式查询';
ALTER TABLE consumer ADD INDEX idx_complaint_count (complaint_count) COMMENT '投诉次数排序';

-- equipment 表补充索引
ALTER TABLE equipment ADD INDEX idx_equipment_status (equipment_status) COMMENT '设备状态筛选';
ALTER TABLE equipment ADD INDEX idx_maintenance_person (maintenance_person(50)) COMMENT '运维责任人查询';
ALTER TABLE equipment ADD INDEX idx_last_inspection (last_inspection_date) COMMENT '最近年检时间查询';

-- ==================== 通知模块相关（新增表） ====================

-- notification 表补充索引（建表时已有，此处补充更多组合查询）
ALTER TABLE notification ADD INDEX idx_user_unread (user_id, is_read) COMMENT '用户未读消息查询';
ALTER TABLE notification ADD INDEX idx_biz (biz_type, biz_id) COMMENT '业务关联查询';

-- ==================== 任务调度相关（新增表） ====================

-- job_execution_log 表补充索引
ALTER TABLE job_execution_log ADD INDEX idx_job_name (job_name) COMMENT '任务名称查询';
ALTER TABLE job_execution_log ADD INDEX idx_duration (duration_ms) COMMENT '耗时排序分析';
