-- ===================================================
-- 低压供电可靠性全流程管控系统 - 数据库初始化脚本
-- 数据库: power_reliability
-- 版本: 1.0.0
-- ===================================================

CREATE DATABASE IF NOT EXISTS power_reliability DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE power_reliability;

-- ==================== 1. 基础台账 ====================

-- 1.1 台区档案表
CREATE TABLE tr_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    area_code VARCHAR(50) NOT NULL COMMENT '台区唯一编码',
    area_name VARCHAR(200) NOT NULL COMMENT '台区名称',
    substation_name VARCHAR(200) COMMENT '所属变电站',
    power_supply_station VARCHAR(200) COMMENT '所属供电所',
    maintenance_team VARCHAR(200) COMMENT '运维班组',
    responsible_person VARCHAR(100) COMMENT '责任客户经理',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    power_supply_scope VARCHAR(500) COMMENT '供电范围',
    commissioning_date DATE COMMENT '投运日期',
    design_capacity DECIMAL(12,2) COMMENT '设计容量(kVA)',
    rated_load DECIMAL(12,2) COMMENT '额定负荷(kW)',
    current_load DECIMAL(12,2) COMMENT '当前在运负荷(kW)',
    supply_radius DECIMAL(8,2) COMMENT '供电半径(米)',
    transformer_model VARCHAR(100) COMMENT '配电变压器型号',
    transformer_manufacturer VARCHAR(200) COMMENT '生产厂家',
    transformer_years INT COMMENT '投运年限',
    total_consumers INT DEFAULT 0 COMMENT '覆盖用户总数',
    residential_count INT DEFAULT 0 COMMENT '居民用户数',
    commercial_count INT DEFAULT 0 COMMENT '工商业用户数',
    area_status TINYINT DEFAULT 1 COMMENT '状态: 0-停运 1-正常 2-改造中',
    last_overhaul_date DATE COMMENT '最近检修日期',
    overhaul_count INT DEFAULT 0 COMMENT '历次检修次数',
    defect_info TEXT COMMENT '缺陷隐患记录',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_area_code (area_code)
) COMMENT '台区档案表';

-- 1.2 低压用户档案表
CREATE TABLE consumer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    consumer_no VARCHAR(50) NOT NULL COMMENT '用户唯一户号',
    consumer_name VARCHAR(200) NOT NULL COMMENT '用户姓名',
    consumer_address VARCHAR(500) COMMENT '用电地址',
    consumer_type TINYINT COMMENT '用电类别: 1-居民 2-一般工商业 3-大工业低压',
    area_id BIGINT COMMENT '所属台区ID',
    area_code VARCHAR(50) COMMENT '所属台区编码',
    line_code VARCHAR(50) COMMENT '所属线路',
    contact_phone VARCHAR(20) COMMENT '联系方式',
    ownership_boundary VARCHAR(200) COMMENT '产权分界点',
    open_date DATE COMMENT '开户时间',
    close_date DATE COMMENT '销户时间',
    load_characteristic VARCHAR(200) COMMENT '用电负荷特性',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-销户 1-在册 2-过户',
    outage_count_year INT DEFAULT 0 COMMENT '年度停电次数累计',
    outage_count_60d INT DEFAULT 0 COMMENT '60天停电次数累计',
    complaint_count INT DEFAULT 0 COMMENT '投诉次数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_consumer_no (consumer_no),
    KEY idx_area_code (area_code),
    KEY idx_consumer_type (consumer_type)
) COMMENT '低压用户档案表';

-- 1.3 线路台账表
CREATE TABLE line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    line_code VARCHAR(50) NOT NULL COMMENT '线路编码',
    line_name VARCHAR(200) NOT NULL COMMENT '线路名称',
    line_type TINYINT COMMENT '线路类型: 1-架空线路 2-电缆线路 3-混合线路',
    voltage_level VARCHAR(20) DEFAULT '0.4kV' COMMENT '电压等级',
    area_id BIGINT COMMENT '所属台区ID',
    area_code VARCHAR(50) COMMENT '所属台区编码',
    start_point VARCHAR(200) COMMENT '起点',
    end_point VARCHAR(200) COMMENT '终点',
    total_length DECIMAL(10,2) COMMENT '总长度(米)',
    line_model VARCHAR(100) COMMENT '导线型号',
    commissioning_date DATE COMMENT '投运时间',
    maintenance_person VARCHAR(100) COMMENT '运维责任人',
    defect_info TEXT COMMENT '缺陷隐患记录',
    fault_count INT DEFAULT 0 COMMENT '故障跳闸次数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-停运 1-运行',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_line_code (line_code),
    KEY idx_area_code (area_code)
) COMMENT '低压线路台账表';

-- 1.4 低压设备台账表
CREATE TABLE equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    equipment_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    equipment_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    equipment_type TINYINT COMMENT '设备类型: 1-配电变压器 2-低压开关 3-刀闸 4-配电箱 5-计量装置 6-分段设备 7-其他',
    equipment_model VARCHAR(100) COMMENT '设备型号',
    specification VARCHAR(200) COMMENT '规格参数',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    manufacture_date DATE COMMENT '出厂日期',
    commissioning_date DATE COMMENT '投运时间',
    last_inspection_date DATE COMMENT '年检时间',
    inspection_cycle INT DEFAULT 365 COMMENT '检修周期(天)',
    area_id BIGINT COMMENT '所属台区ID',
    area_code VARCHAR(50) COMMENT '所属台区编码',
    line_code VARCHAR(50) COMMENT '所属线路',
    location_desc VARCHAR(500) COMMENT '位置描述',
    maintenance_person VARCHAR(100) COMMENT '运维责任人',
    defect_info TEXT COMMENT '缺陷隐患记录',
    fault_count INT DEFAULT 0 COMMENT '故障次数',
    replacement_count INT DEFAULT 0 COMMENT '更换次数',
    equipment_status TINYINT DEFAULT 1 COMMENT '状态: 0-停运 1-运行 2-待维修 3-报废',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_equipment_code (equipment_code),
    KEY idx_area_code (area_code),
    KEY idx_equipment_type (equipment_type)
) COMMENT '低压设备台账表';

-- 1.5 设备缺陷记录表
CREATE TABLE equipment_defect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    equipment_id BIGINT COMMENT '设备ID',
    equipment_code VARCHAR(50) COMMENT '设备编号',
    defect_type TINYINT COMMENT '缺陷类型: 1-一般 2-严重 3-危急',
    defect_desc TEXT COMMENT '缺陷描述',
    discovery_date DATETIME COMMENT '发现时间',
    discovery_person VARCHAR(100) COMMENT '发现人',
    repair_status TINYINT DEFAULT 0 COMMENT '处理状态: 0-未处理 1-处理中 2-已处理',
    repair_date DATETIME COMMENT '处理时间',
    repair_person VARCHAR(100) COMMENT '处理人',
    repair_measures TEXT COMMENT '处理措施',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_equipment (equipment_id),
    KEY idx_defect_type (defect_type)
) COMMENT '设备缺陷记录表';

-- ==================== 2. 停电事件管理 ====================

-- 2.1 停电事件主表
CREATE TABLE outage_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_no VARCHAR(50) NOT NULL COMMENT '事件编号',
    outage_type TINYINT NOT NULL COMMENT '停电类型: 1-计划停电 2-临时停电 3-故障停电 4-外力破坏 5-极端天气 6-用户侧故障',
    outage_start_time DATETIME COMMENT '停电开始时间',
    outage_end_time DATETIME COMMENT '复电时间',
    outage_duration INT COMMENT '停电时长(分钟)',
    area_id BIGINT COMMENT '涉及台区ID',
    area_code VARCHAR(50) COMMENT '涉及台区编码',
    area_name VARCHAR(200) COMMENT '涉及台区名称',
    affected_consumer_count INT DEFAULT 0 COMMENT '影响用户数',
    affected_consumer_ids TEXT COMMENT '影响用户ID列表',
    fault_type VARCHAR(100) COMMENT '故障类型',
    fault_reason TEXT COMMENT '故障原因',
    fault_location VARCHAR(500) COMMENT '故障位置',
    is_exempt TINYINT DEFAULT 0 COMMENT '是否豁免: 0-否 1-是',
    exempt_type TINYINT COMMENT '豁免类型: 1-不可抗力 2-外力破坏 3-用户产权故障 4-其他合规豁免',
    exempt_basis TEXT COMMENT '豁免依据',
    is_closed TINYINT DEFAULT 0 COMMENT '是否闭环: 0-否 1-是',
    close_time DATETIME COMMENT '闭环时间',
    is_archived TINYINT DEFAULT 0 COMMENT '是否归档: 0-否 1-是',
    remarks TEXT COMMENT '备注',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_no (event_no),
    KEY idx_area_code (area_code),
    KEY idx_outage_type (outage_type),
    KEY idx_start_time (outage_start_time),
    KEY idx_is_exempt (is_exempt)
) COMMENT '停电事件主表';

-- 2.2 计划停电审批表
CREATE TABLE planned_outage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    plan_start_time DATETIME NOT NULL COMMENT '计划停电时间',
    plan_end_time DATETIME NOT NULL COMMENT '计划复电时间',
    actual_start_time DATETIME COMMENT '实际停电时间',
    actual_end_time DATETIME COMMENT '实际复电时间',
    outage_content TEXT COMMENT '停电施工内容',
    construction_team VARCHAR(200) COMMENT '施工班组',
    responsible_person VARCHAR(100) COMMENT '责任人',
    outage_scope VARCHAR(500) COMMENT '停电范围',
    construction_plan TEXT COMMENT '施工方案',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态: 0-待审批 1-审批中 2-已通过 3-已驳回 4-已取消',
    approval_level INT DEFAULT 0 COMMENT '当前审批层级',
    approval_record TEXT COMMENT '审批记录JSON',
    compliance_check TINYINT DEFAULT 1 COMMENT '合规校验: 0-不通过 1-通过',
    compliance_detail TEXT COMMENT '合规校验详情',
    is_overtime TINYINT DEFAULT 0 COMMENT '是否超时: 0-否 1-是',
    overtime_minutes INT DEFAULT 0 COMMENT '超时时长(分钟)',
    is_over_scope TINYINT DEFAULT 0 COMMENT '是否超范围: 0-否 1-是',
    execution_status TINYINT DEFAULT 0 COMMENT '执行状态: 0-未开始 1-执行中 2-已完成',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_event_id (event_id),
    KEY idx_approval_status (approval_status)
) COMMENT '计划停电审批表';

-- 2.3 故障停电登记表
CREATE TABLE fault_outage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    fault_source TINYINT COMMENT '故障来源: 1-系统自动抓取 2-人工上报 3-用户报修',
    source_detail VARCHAR(200) COMMENT '来源详情',
    fault_time DATETIME COMMENT '故障发生时间',
    fault_type VARCHAR(100) COMMENT '故障类型: 跳闸/失压/断相/其他',
    fault_level TINYINT COMMENT '故障级别: 1-一般 2-严重 3-紧急',
    trip_device VARCHAR(200) COMMENT '跳闸设备',
    protection_action VARCHAR(200) COMMENT '保护动作',
    recovery_time DATETIME COMMENT '恢复供电时间',
    recovery_method VARCHAR(200) COMMENT '恢复方式',
    data_source VARCHAR(100) COMMENT '数据来源(对接系统)',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_event_id (event_id),
    KEY idx_fault_time (fault_time),
    KEY idx_fault_level (fault_level)
) COMMENT '故障停电登记表';

-- 2.4 抢修工单表
CREATE TABLE repair_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no VARCHAR(50) NOT NULL COMMENT '工单编号',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    dispatch_time DATETIME COMMENT '派单时间',
    arrive_time DATETIME COMMENT '抵达现场时间',
    repair_team VARCHAR(200) COMMENT '抢修班组',
    repair_persons VARCHAR(500) COMMENT '抢修人员',
    fault_finding TEXT COMMENT '故障排查情况',
    repair_measures TEXT COMMENT '抢修处置措施',
    repair_complete_time DATETIME COMMENT '抢修完成时间',
    recovery_verify_time DATETIME COMMENT '复电核验时间',
    verify_person VARCHAR(100) COMMENT '核验人',
    verify_result TINYINT COMMENT '核验结果: 0-未核验 1-通过 2-不通过',
    fault_evidence JSON COMMENT '故障佐证资料(照片URL等)',
    order_status TINYINT DEFAULT 0 COMMENT '工单状态: 0-待派单 1-已派单 2-处理中 3-已完成 4-已核验',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_event_id (event_id),
    KEY idx_order_status (order_status)
) COMMENT '抢修工单表';

-- 2.5 停电豁免判定表
CREATE TABLE outage_exemption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    exempt_type TINYINT NOT NULL COMMENT '豁免类型: 1-不可抗力 2-外力破坏 3-用户产权故障 4-其他',
    exempt_reason TEXT COMMENT '豁免原因',
    exempt_basis TEXT COMMENT '豁免依据',
    weather_data JSON COMMENT '气象数据(不可抗力)',
    damage_evidence JSON COMMENT '外力破坏佐证资料',
    user_fault_proof JSON COMMENT '用户侧故障凭证',
    verdict_person VARCHAR(100) COMMENT '判定人',
    verdict_time DATETIME COMMENT '判定时间',
    verdict_status TINYINT DEFAULT 0 COMMENT '判定状态: 0-待判定 1-已豁免 2-不豁免',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_event_id (event_id)
) COMMENT '停电豁免判定表';

-- 2.6 停电事件归档表
CREATE TABLE outage_archive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    archive_type TINYINT COMMENT '归档类型: 1-停电档案',
    archive_content JSON COMMENT '归档内容JSON(含全过程数据)',
    archive_size INT COMMENT '归档大小(KB)',
    archive_time DATETIME COMMENT '归档时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_event_id (event_id),
    KEY idx_archive_time (archive_time)
) COMMENT '停电事件归档表';

-- ==================== 3. 频繁停电治理 ====================

-- 3.1 频繁停电台账表
CREATE TABLE frequent_outage_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    area_name VARCHAR(200) COMMENT '台区名称',
    violation_type TINYINT COMMENT '超标类型: 1-年停电超5次 2-60天停电超3次 3-两者均超',
    current_year_count INT DEFAULT 0 COMMENT '本年度停电次数',
    last_60d_count INT DEFAULT 0 COMMENT '近60天停电次数',
    risk_level TINYINT COMMENT '风险等级: 1-一级严重超标 2-二级一般超标 3-三级临界风险',
    main_cause VARCHAR(500) COMMENT '主要诱因',
    governance_status TINYINT DEFAULT 0 COMMENT '治理状态: 0-未治理 1-治理中 2-已治理 3-复核中 4-已销号',
    governance_start_date DATE COMMENT '治理开始日期',
    governance_end_date DATE COMMENT '治理完成日期',
    is_recurrence TINYINT DEFAULT 0 COMMENT '是否反弹: 0-否 1-是',
    recurrence_count INT DEFAULT 0 COMMENT '反弹次数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_area_code_violation (area_code, violation_type),
    KEY idx_risk_level (risk_level),
    KEY idx_governance_status (governance_status)
) COMMENT '频繁停电台账表';

-- 3.2 停电归因分析表
CREATE TABLE outage_cause_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    event_id BIGINT COMMENT '停电事件ID',
    event_no VARCHAR(50) COMMENT '事件编号',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    cause_category VARCHAR(100) COMMENT '归因类别',
    cause_detail TEXT COMMENT '归因详情',
    cause_type TINYINT COMMENT '原因类型: 1-设备老化缺陷 2-线路隐患 3-接头发热 4-负荷过载 5-三相不平衡 6-恶劣天气 7-外力破坏 8-运维不到位',
    evidence_data JSON COMMENT '归因证据数据',
    analysis_result TEXT COMMENT '分析结论',
    direct_cause TEXT COMMENT '直接原因',
    root_cause TEXT COMMENT '根本原因',
    responsibility_unit VARCHAR(200) COMMENT '责任单位',
    analyst VARCHAR(100) COMMENT '分析人',
    confidence DECIMAL(5,2) COMMENT '置信度(%)',
    remarks TEXT COMMENT '备注',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_event_id (event_id),
    KEY idx_area_code (area_code),
    KEY idx_cause_type (cause_type)
) COMMENT '停电归因分析表';

-- 3.3 频繁停电治理工单表
CREATE TABLE governance_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no VARCHAR(50) NOT NULL COMMENT '治理工单编号',
    ledger_id BIGINT COMMENT '频繁停电台账ID',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    area_name VARCHAR(200) COMMENT '台区名称',
    problem_desc TEXT COMMENT '问题描述',
    cause_analysis TEXT COMMENT '归因分析',
    governance_measures TEXT COMMENT '治理措施',
    governance_standard TEXT COMMENT '治理标准',
    responsible_team VARCHAR(200) COMMENT '责任班组',
    responsible_person VARCHAR(100) COMMENT '责任人',
    deadline_date DATE COMMENT '治理时限',
    urgency_level TINYINT DEFAULT 2 COMMENT '紧急程度: 1-紧急 2-一般',
    order_status TINYINT DEFAULT 0 COMMENT '工单状态: 0-待派发 1-治理中 2-待验收 3-已验收通过 4-退回整改 5-已销号',
    submitted_evidence JSON COMMENT '提交佐证资料',
   验收_person VARCHAR(100) COMMENT '验收人',
   验收_time DATETIME COMMENT '验收时间',
   验收_result TINYINT COMMENT '验收结果: 0-待验收 1-通过 2-不通过',
   验收_comment TEXT COMMENT '验收意见',
    is_appeal TINYINT DEFAULT 0 COMMENT '是否超期: 0-否 1-是',
    appeal_days INT DEFAULT 0 COMMENT '超期天数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_ledger_id (ledger_id),
    KEY idx_area_code (area_code),
    KEY idx_order_status (order_status)
) COMMENT '频繁停电治理工单表';

-- 3.4 治理成效跟踪表
CREATE TABLE governance_effect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    ledger_id BIGINT COMMENT '频繁停电台账ID',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    governance_id BIGINT COMMENT '治理工单ID',
    before_outage_count_30d INT DEFAULT 0 COMMENT '治理前30天停电次数',
    before_outage_count_60d INT DEFAULT 0 COMMENT '治理前60天停电次数',
    before_outage_count_year INT DEFAULT 0 COMMENT '治理前年度停电次数',
    after_outage_count_30d INT DEFAULT 0 COMMENT '治理后30天停电次数',
    after_outage_count_60d INT DEFAULT 0 COMMENT '治理后60天停电次数',
    after_outage_count_90d INT DEFAULT 0 COMMENT '治理后90天停电次数',
    effect_assessment TINYINT COMMENT '成效评估: 1-显著改善 2-有所改善 3-无明显变化 4-反弹',
    assessment_time DATETIME COMMENT '评估时间',
    assessment_person VARCHAR(100) COMMENT '评估人',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_ledger_id (ledger_id),
    KEY idx_area_code (area_code)
) COMMENT '治理成效跟踪表';

-- ==================== 4. 可靠性指标 ====================

-- 4.1 可靠性指标统计表
CREATE TABLE reliability_index (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    stat_type TINYINT NOT NULL COMMENT '统计类型: 1-全域 2-供电所 3-班组 4-台区',
    stat_target_id BIGINT COMMENT '统计目标ID',
    stat_target_name VARCHAR(200) COMMENT '统计目标名称',
    stat_period TINYINT NOT NULL COMMENT '统计周期: 1-日 2-周 3-月 4-季 5-半年 6-年',
    stat_start_date DATE NOT NULL COMMENT '统计开始日期',
    stat_end_date DATE NOT NULL COMMENT '统计结束日期',
    saidi DECIMAL(10,4) COMMENT 'SAIDI(用户平均停电持续时间/分钟)',
    saifi DECIMAL(10,4) COMMENT 'SAIFI(用户平均停电次数)',
    avg_outage_duration DECIMAL(10,2) COMMENT '平均每次停电持续时间(分钟)',
    fault_recovery_rate DECIMAL(5,2) COMMENT '故障复电成功率(%)',
    avg_fault_recovery_time DECIMAL(10,2) COMMENT '平均故障复电时长(分钟)',
    planned_compliance_rate DECIMAL(5,2) COMMENT '计划停电合规率(%)',
    area_violation_rate DECIMAL(5,2) COMMENT '台区停电超标率(%)',
    frequent_clear_rate DECIMAL(5,2) COMMENT '频繁停电清零率(%)',
    total_outage_count INT DEFAULT 0 COMMENT '停电总次数',
    planned_outage_count INT DEFAULT 0 COMMENT '计划停电次数',
    fault_outage_count INT DEFAULT 0 COMMENT '故障停电次数',
    exempt_outage_count INT DEFAULT 0 COMMENT '豁免停电次数',
    total_affected_consumers INT DEFAULT 0 COMMENT '累计影响用户数',
    total_outage_minutes INT DEFAULT 0 COMMENT '累计停电分钟数',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stat (stat_type, stat_target_id, stat_period, stat_start_date, stat_end_date),
    KEY idx_stat_period (stat_period),
    KEY idx_stat_date (stat_start_date)
) COMMENT '可靠性指标统计表';

-- 4.2 指标异常预警表
CREATE TABLE index_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    index_type VARCHAR(50) COMMENT '指标类型',
    index_value DECIMAL(10,4) COMMENT '当前值',
    threshold_value DECIMAL(10,4) COMMENT '阈值',
    alert_level TINYINT COMMENT '预警级别: 1-警告 2-严重 3-危急',
    stat_target_name VARCHAR(200) COMMENT '统计目标',
    stat_period_desc VARCHAR(100) COMMENT '统计周期描述',
    trend_direction TINYINT COMMENT '趋势: 1-上升 2-下降',
    alert_reason TEXT COMMENT '预警原因',
    is_handled TINYINT DEFAULT 0 COMMENT '是否已处理',
    handled_time DATETIME COMMENT '处理时间',
    handler VARCHAR(100) COMMENT '处理人',
    handling_measures TEXT COMMENT '处理措施',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_alert_level (alert_level),
    KEY idx_is_handled (is_handled)
) COMMENT '指标异常预警表';

-- ==================== 5. 隐患预判与预警 ====================

-- 5.1 隐患预判记录表
CREATE TABLE risk_prediction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    area_name VARCHAR(200) COMMENT '台区名称',
    risk_type TINYINT COMMENT '风险类型: 1-超负荷 2-三相不平衡 3-设备老化 4-线路老旧 5-接头发热 6-瞬时跳闸 7-雷雨易故障 8-外力施工高发',
    risk_score DECIMAL(5,2) COMMENT '风险评分(0-100)',
    risk_level TINYINT COMMENT '风险等级: 1-高危 2-中风险 3-低风险',
    risk_factors JSON COMMENT '风险因子数据',
    prediction_desc TEXT COMMENT '预判描述',
    predicted_fault_type VARCHAR(100) COMMENT '预判故障类型',
    recommended_action TEXT COMMENT '推荐处置建议',
    prediction_time DATETIME COMMENT '预判时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_area_code (area_code),
    KEY idx_risk_level (risk_level),
    KEY idx_prediction_time (prediction_time)
) COMMENT '隐患预判记录表';

-- 5.2 隐患预警工单表
CREATE TABLE risk_warning_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    prediction_id BIGINT COMMENT '隐患预判记录ID',
    area_id BIGINT COMMENT '台区ID',
    area_code VARCHAR(50) COMMENT '台区编码',
    risk_type TINYINT COMMENT '风险类型',
    risk_level TINYINT COMMENT '风险等级',
    warning_desc TEXT COMMENT '预警描述',
    warning_time DATETIME COMMENT '预警时间',
    push_channel VARCHAR(100) COMMENT '推送渠道: system/app/sms',
    receiver VARCHAR(500) COMMENT '接收人',
    response_deadline DATETIME COMMENT '处置时限',
    order_status TINYINT DEFAULT 0 COMMENT '工单状态: 0-待处置 1-处置中 2-已处置 3-已复核',
    disposal_desc TEXT COMMENT '处置描述',
    disposal_person VARCHAR(100) COMMENT '处置人',
    disposal_time DATETIME COMMENT '处置时间',
    review_person VARCHAR(100) COMMENT '复核人',
    review_time DATETIME COMMENT '复核时间',
    review_result TINYINT COMMENT '复核结果: 0-待复核 1-通过 2-不通过',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_prediction_id (prediction_id),
    KEY idx_area_code (area_code),
    KEY idx_order_status (order_status)
) COMMENT '隐患预警工单表';

-- ==================== 6. 复盘整改 ====================

-- 6.1 停电复盘报告表
CREATE TABLE review_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    report_no VARCHAR(50) NOT NULL COMMENT '报告编号',
    report_type TINYINT COMMENT '报告类型: 1-日复盘 2-周复盘 3-月复盘 4-季度复盘 5-专项复盘',
    report_title VARCHAR(500) COMMENT '报告标题',
    report_period_start DATE COMMENT '复盘开始日期',
    report_period_end DATE COMMENT '复盘结束日期',
    related_event_ids TEXT COMMENT '关联事件ID列表',
    summary TEXT COMMENT '复盘总结',
    problem_list JSON COMMENT '问题清单',
    root_cause TEXT COMMENT '根因分析',
    improvement_measures TEXT COMMENT '改进措施',
    responsibility_dept VARCHAR(200) COMMENT '责任部门',
    report_status TINYINT DEFAULT 0 COMMENT '报告状态: 0-草稿 1-已发布 2-已归档',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_no (report_no),
    KEY idx_report_type (report_type),
    KEY idx_report_period (report_period_start)
) COMMENT '停电复盘报告表';

-- 6.2 整改任务表
CREATE TABLE rectification_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_no VARCHAR(50) NOT NULL COMMENT '任务编号',
    review_id BIGINT COMMENT '复盘报告ID',
    problem_desc TEXT COMMENT '问题描述',
    root_cause TEXT COMMENT '根因',
    rectification_measures TEXT COMMENT '整改措施',
    responsible_person VARCHAR(100) COMMENT '整改责任人',
    responsible_dept VARCHAR(200) COMMENT '责任部门',
    deadline_date DATE COMMENT '整改时限',
    acceptance_standard TEXT COMMENT '验收标准',
    task_status TINYINT DEFAULT 0 COMMENT '任务状态: 0-待整改 1-整改中 2-待验收 3-已验收通过 4-退回',
    rectification_desc TEXT COMMENT '整改情况',
    rectification_person VARCHAR(100) COMMENT '整改人',
    rectification_time DATETIME COMMENT '整改完成时间',
    acceptance_person VARCHAR(100) COMMENT '验收人',
    acceptance_time DATETIME COMMENT '验收时间',
    acceptance_result TINYINT COMMENT '验收结果: 0-待验收 1-通过 2-不通过',
    acceptance_comment TEXT COMMENT '验收意见',
    is_repeated TINYINT DEFAULT 0 COMMENT '是否重复问题',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_review_id (review_id),
    KEY idx_task_status (task_status)
) COMMENT '整改任务表';

-- ==================== 7. 绩效考核 ====================

-- 7.1 绩效考核记录表
CREATE TABLE performance_assessment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    assessment_period TINYINT COMMENT '考核周期: 1-月 2-季 3-年',
    assessment_year INT COMMENT '考核年份',
    assessment_month INT COMMENT '考核月份',
    assessment_target_type TINYINT COMMENT '考核对象类型: 1-供电所 2-运维班组 3-个人',
    assessment_target_id BIGINT COMMENT '考核对象ID',
    assessment_target_name VARCHAR(200) COMMENT '考核对象名称',
    area_violation_rate DECIMAL(5,2) COMMENT '台区停电超标率',
    governance_closure_rate DECIMAL(5,2) COMMENT '治理闭环率',
    warning_disposal_rate DECIMAL(5,2) COMMENT '隐患预警处置及时率',
    avg_repair_time DECIMAL(10,2) COMMENT '平均抢修时长(分钟)',
    outage_compliance_rate DECIMAL(5,2) COMMENT '停电合规率',
    complaint_rate DECIMAL(5,2) COMMENT '停电投诉率',
    ledger_compliance_rate DECIMAL(5,2) COMMENT '台账合规率',
    total_score DECIMAL(5,2) COMMENT '综合评分',
    assessment_grade VARCHAR(20) COMMENT '考核等级: A/B/C/D',
    assessment_comment TEXT COMMENT '考核评语',
    assessment_status TINYINT DEFAULT 0 COMMENT '状态: 0-待评估 1-已评估 2-已确认',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_assessment_target (assessment_target_type, assessment_target_id),
    KEY idx_assessment_period (assessment_period, assessment_year, assessment_month)
) COMMENT '绩效考核记录表';

-- ==================== 8. 系统权限 ====================

-- 8.1 系统用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(500) NOT NULL COMMENT '密码',
    real_name VARCHAR(200) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(200) COMMENT '邮箱',
    dept_id BIGINT COMMENT '部门ID',
    post VARCHAR(100) COMMENT '岗位',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) COMMENT '系统用户表';

-- 8.2 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色标识',
    role_sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_key (role_key)
) COMMENT '角色表';

-- 8.3 菜单表
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_type TINYINT COMMENT '类型: 0-目录 1-菜单 2-按钮',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    permission_key VARCHAR(200) COMMENT '权限标识',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见: 0-隐藏 1-可见',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent_id (parent_id)
) COMMENT '菜单表';

-- 8.4 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 8.5 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联表';

-- 8.6 部门表
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dept_name VARCHAR(200) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    dept_type TINYINT COMMENT '部门类型: 1-供电公司 2-供电所 3-班组',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent_id (parent_id)
) COMMENT '部门表';

-- 8.7 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    module VARCHAR(100) COMMENT '操作模块',
    action VARCHAR(100) COMMENT '操作类型',
    target_id VARCHAR(100) COMMENT '操作对象ID',
    target_desc VARCHAR(500) COMMENT '操作对象描述',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    operation_time DATETIME COMMENT '操作时间',
    duration_ms INT COMMENT '耗时(毫秒)',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_operation_time (operation_time)
) COMMENT '操作日志表';

-- ==================== 9. 系统配置 ====================

-- 9.1 系统配置表
CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_key VARCHAR(200) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_desc VARCHAR(500) COMMENT '配置说明',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '系统配置表';

-- 9.2 字典表
CREATE TABLE sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(200) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(200) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_dict_type (dict_type)
) COMMENT '字典表';

-- 9.3 消息通知配置表
CREATE TABLE sys_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    notify_type VARCHAR(50) COMMENT '通知类型: risk_warning/order_overtime/index_alert',
    push_channel VARCHAR(100) COMMENT '推送渠道: system/app/sms',
    receiver_ids TEXT COMMENT '接收人ID列表',
    template_content TEXT COMMENT '模板内容',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_notify_type (notify_type)
) COMMENT '消息通知配置表';

-- ==================== 初始化数据 ====================

-- 默认管理员
INSERT INTO sys_user (username, password, real_name, phone, email, dept_id, post, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', '13800000000', 'admin@power.cn', 1, '管理员', 1);

-- 默认角色
INSERT INTO sys_role (role_name, role_key, role_sort, status) VALUES
('超级管理员', 'super_admin', 1, 1),
('供电所管理人员', 'station_manager', 2, 1),
('运维班组长', 'team_leader', 3, 1),
('运维人员', 'operator', 4, 1),
('合规审计人员', 'auditor', 5, 1);

-- 默认管理员角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 默认系统配置
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
('system_name', '低压供电可靠性全流程管控系统', '系统名称'),
('company_name', '同业电力', '企业名称'),
('version', '1.0.0', '系统版本'),
('safe_outage_count_60d', '3', '60天停电管控阈值'),
('safe_outage_count_year', '5', '年度停电管控阈值'),
('warning_risk_score_high', '80', '高危风险阈值'),
('warning_risk_score_medium', '50', '中风险阈值'),
('governance_deadline_high', '24', '高危隐患处置时限(小时)'),
('governance_deadline_medium', '48', '中风险处置时限(小时)');

-- 默认字典
INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort_order) VALUES
('outage_type', '计划停电', '1', 1),
('outage_type', '临时停电', '2', 2),
('outage_type', '故障停电', '3', 3),
('outage_type', '外力破坏', '4', 4),
('outage_type', '极端天气', '5', 5),
('outage_type', '用户侧故障', '6', 6),
('risk_level', '高危', '1', 1),
('risk_level', '中风险', '2', 2),
('risk_level', '低风险', '3', 3),
('cause_type', '设备老化缺陷', '1', 1),
('cause_type', '线路隐患', '2', 2),
('cause_type', '接头发热故障', '3', 3),
('cause_type', '负荷过载', '4', 4),
('cause_type', '三相不平衡', '5', 5),
('cause_type', '恶劣天气影响', '6', 6),
('cause_type', '外力施工破坏', '7', 7),
('cause_type', '运维巡检不到位', '8', 8);
