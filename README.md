# 低压供电可靠性全流程管控系统

> 基于 Spring Cloud Alibaba 微服务架构，聚焦 **0.4kV 低压配网供电可靠性全流程管控**，全面适配 2025 年国家发改委、能源局供电可靠性新政要求（5 次/年、60 天/3 次频次红线）。

**代码规模**: 588 个源文件 · 约 70,000 行代码（后端 15K + 前端 55K）  
**数据库**: 34 张业务表  
**前端**: 12 个功能模块 · 44+ 个页面  
**许可证**: Copyright © 2026

---

## 📋 目录

- [系统架构](#系统架构)
- [核心业务流程](#核心业务流程)
- [模块详解](#模块详解)
- [技术栈](#技术栈)
- [数据库概览](#数据库概览)
- [定时任务](#定时任务)
- [自研引擎](#自研引擎)
- [API 接口总览](#api-接口总览)
- [前端功能模块](#前端功能模块)
- [快速开始](#快速开始)
- [配置参考](#配置参考)
- [开发指南](#开发指南)
- [运维要点](#运维要点)

---

## 系统架构

```
┌──────────────────────────────────────────────────────┐
│                   前端 (Vue 2 + Element UI)            │
│  台账 / 停电 / 治理 / 指标 / 预警 / 看板 / 系统管理      │
└──────────────────────┬───────────────────────────────┘
                       │ HTTP/HTTPS
┌──────────────────────▼───────────────────────────────┐
│              API 网关 (Spring Cloud Gateway)           │
│           Sentinel 限流 · JWT 鉴权 · 路由转发            │
└────┬──────┬──────┬──────┬──────┬──────┬──────┬───────┘
     │      │      │      │      │      │      │
┌────▼──┐┌─▼─────┐┌─▼────┐┌─▼───┐┌─▼────┐┌─▼───┐┌─▼────┐
│ led- ││outage ││gov-  ││index││warn-││review││dash- │
│ ger  ││       ││ern-  ││     ││ing  ││      ││board │
│台账   ││停电管控││治理   ││指标  ││预警  ││复盘   ││看板  │
│      ││       ││      ││     ││     ││考核   ││     │
└──────┘└───────┘└──────┘└─────┘└─────┘└──────┘└─────┘
     │      │      │      │      │      │      │
     └──────┴──────┴──────┴──────┴──────┴──────┘
                       │
              ┌────────▼────────┐
              │   Nacos 注册中心  │
              │   (配置中心 + 服务发现)  │
              └────────┬────────┘
                       │
              ┌────────▼────────┐
              │ MySQL 8.0 + Redis 6.0 │
              └─────────────────┘
```

### 模块依赖关系

```
                         power-reliability-gateway
                                │
                    ┌───────────┴───────────┐
                    │                       │
          power-reliability-api      power-reliability-system-srv
          (外部系统对接)                 (系统权限/认证)
                    │
    ┌───────────────┼───────────────┐
    │               │               │
power-reliability-common (公共/引擎/工具)
    ▲               ▲               ▲
    │               │               │
 ┌──┴──┐  ┌─────────┴────────┐  ┌──┴──────────┐
 │ledger│  │outage  governance│  │index warning│
 │      │  │review  dashboard│  │notification │
 └──────┘  └─────────────────┘  └─────────────┘
                                    │
                            power-reliability-job
                            (定时任务: 指标/预警/归档/成效跟踪)
```

---

## 核心业务流程

### 1️⃣ 停电事件 → 合规校验 → 治理闭环

```
停电事件发生
    │
    ▼
┌─────────────────────┐
│  频次合规校验        │  ← PolicyRuleEngine: 5次/年、60天/3次
│  (台区/用户维度)      │
└─────────┬───────────┘
          │ 超标?
     ┌────┴────┐
     ▼         ▼
  合规      超标
              │
              ▼
    ┌─────────────────┐
    │ 频繁停电治理台账   │  ← FrequentOutageLedger
    │ 归因分析(8大诱因)  │  ← OutageCauseEngine
    │ 派发治理工单       │  ← GovernanceOrder
    │ 工单审批流转       │  ← WorkflowEngine(支撑)
    │ 现场执行           │
    │ 复电核验           │
    └─────────┬─────────┘
              │
              ▼
    ┌─────────────────┐
    │ 成效跟踪(30/60/90天)│  ← GovernanceEffectTrackJob
    │ 无复发 → 闭环归档  │
    └─────────────────┘
```

### 2️⃣ 指标核算 → 预警 → 报表

```
停电事件闭环
    │
    ▼
┌──────────────────────┐
│ 每日凌晨指标计算       │  ← IndexCalculateJob (02:00)
│ SAIDI / SAIFI / CAIDI │
│       / ASAI          │
│ (全局 + 分台区)        │
└──────────┬───────────┘
           │
     ┌─────┴─────┐
     ▼           ▼
  正常         超阈值
                │
                ▼
       ┌──────────────┐
       │ 创建预警通知    │  ← AlertCheckJob (每30分钟)
       │ Notification表 │
       │ → 通知消费    │  ← NotificationSendJob (每5分钟)
       └──────┬───────┘
              │
              ▼
       ┌──────────────┐
       │ 月度/季度报表   │  ← 标准化报表模板
       │ Excel导出      │
       └──────────────┘
```

---

## 模块详解

### power-reliability-common（公共模块）
文件: 12 个 · 852 行

核心内容:
| 组件 | 说明 |
|------|------|
| `common.entity.Result` | 统一 API 返回体（泛型） |
| `common.entity.PageResult` | 分页返回体 |
| `common.annotation.Excel` | Excel 导出字段注解 |
| `common.util.ExcelExportUtil` | 通用 Excel 导出工具（SXSSF 流式写入） |
| `common.config.MybatisPlusConfig` | 分页插件配置 |
| `common.config.MyMetaObjectHandler` | 26 个实体 createTime/updateTime 自动填充 |
| `common.config.GlobalExceptionHandler` | 全局异常统一处理 |
| `common.engine.PolicyRuleEngine` | **政策合规规则引擎**（5次/年、60天/3次阈值） |
| `common.engine.OutageCauseEngine` | **停电智能归因引擎**（8类标准诱因加权评分） |
| `common.engine.WorkflowEngine` | **全流程业务闭环引擎**（7节点审批流转） |

---

### power-relicability-gateway（API 网关）
文件: 3 个 · 283 行

- **技术**: Spring Cloud Gateway + Sentinel
- **端口**: 8080（默认）
- **功能**:
  - 路由转发到各微服务
  - JWT Token 鉴权（`Authorization: Bearer`）
  - Sentinel 限流（全局 1000 QPS，各模块 200-500 QPS）
  - StripPrefix 1（移除 `/api` 前缀）
- **限流规则**:
  | API 分组 | QPS 上限 |
  |----------|---------|
  | `/**` 全局 | 1000 |
  | `/api/dashboard/**` | 500 |
  | `/api/ledger/**` | 200 |
  | `/api/outage/**` | 200 |
  | `/api/governance/**` | 200 |

---

### power-reliability-ledger（台账管理）
文件: 30 个 · 1,664 行 · 上限最大模块

| 实体 | 表名 | 说明 |
|------|------|------|
| TrArea | `tr_area` | 台区（配变）档案 |
| Consumer | `consumer` | 用电用户档案 |
| Equipment | `equipment` | 设备台账 |
| Line | `line` | 线路档案 |
| LedgerValidationRecord | `ledger_validation_record` | 台账合规校验记录 |

**API 端点**:
| 端点 | 说明 |
|------|------|
| `GET/POST /api/ledger/area` | 台区 CRUD |
| `GET/POST /api/ledger/consumer` | 用户 CRUD |
| `GET/POST /api/ledger/equipment` | 设备 CRUD |
| `GET/POST /api/ledger/line/**` | 线路 CRUD |
| `POST /api/ledger/validate/validate-all` | 触发全量校验 |
| `GET /api/ledger/validate/list` | 校验记录查询 |
| `POST /api/ledger/validate/export` | 校验结果导出 |

**三级校验规则**:
- ⭐ **完整性校验**: areaCode/areaName/substationName 非空验证
- ⭐ **逻辑性校验**: commissioningDate(start) ≤ commissioningDate(end)
- ⭐ **规范性校验**: currentLoad ≤ ratedLoad（当前负荷不超过额定容量）

---

### power-reliability-outage（停电管控）
文件: 30 个 · 1,363 行

| 实体 | 表名 | 说明 |
|------|------|------|
| OutageEvent | `outage_event` | 停电事件主表 |
| PlannedOutage | `planned_outage` | 计划停电 |
| FaultOutage | `fault_outage` | 故障停电 |
| OutageExemption | `outage_exemption` | 豁免停电（不计入频次统计） |
| RepairOrder | `repair_order` | 抢修工单 |
| OutageArchive | `outage_archive` | 停电归档 |

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/outage/**` | 停电事件 CRUD + 列表/导出 |
| `/api/outage/planned/**` | 计划停电管理 |
| `/api/outage/fault/**` | 故障停电管理 |
| `/api/outage/exemption/**` | 豁免停电管理 |
| `/api/outage/repair/**` | 抢修工单管理 |

**频次合规校验（业务核心）**:
- 单台区年停电 ≤ 5 次
- 单台区 60 天内停电 ≤ 3 次
- 超过即自动标记为"频繁停电"，进入治理流程
- 豁免事件不计入频次

---

### power-reliability-governance（频繁停电治理）
文件: 21 个 · 1,169 行

| 实体 | 表名 | 说明 |
|------|------|------|
| FrequentOutageLedger | `frequent_outage_ledger` | 频繁停电治理台账 |
| GovernanceOrder | `governance_order` | 治理工单 |
| GovernanceEffect | `governance_effect` | 治理成效记录 |
| OutageCauseAnalysis | `outage_cause_analysis` | 停电归因分析 |

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/governance/ledger/**` | 治理台账 CRUD |
| `/api/governance/order/**` | 治理工单审批流转 |
| `/api/governance/cause/list` | 归因分析查询 |
| `/api/governance/cause/analyze/{eventId}` | 触发归因分析 |
| `/api/governance/cause/export` | 归因结果导出 Excel |

**工单审批流转**:
```
申报 → 班组审批 → 供电所审批 → 公司审批 → 现场执行 → 复电核验 → 归档
```

**治理成效跟踪**:
- 工单归档后自动开启 30 天 / 60 天 / 90 天跟踪
- Git 定期核验断电次数是否下降
- 跟踪期内无复发 → 闭环结案

---

### power-reliability-index（指标核算）
文件: 12 个 · 1,280 行

| 实体 | 表名 | 说明 |
|------|------|------|
| ReliabilityIndex | `reliability_index` | 可靠性指标数据 |
| IndexAlert | `index_alert` | 指标异常预警 |
| ReportTemplate | `report_template` | 标准化报表模板 |

**核心指标**:
| 指标 | 全称 | 含义 |
|------|------|------|
| SAIDI | System Average Interruption Duration Index | 系统平均停电持续时间（分钟） |
| SAIFI | System Average Interruption Frequency Index | 系统平均停电频率（次/户） |
| CAIDI | Customer Average Interruption Duration Index | 用户平均停电持续时间（分钟） |
| ASAI | Average Service Availability Index | 平均供电可用率（%） |
| ENS | Energy Not Supplied | 缺供电量（kWh） |
| AENS | Average Energy Not Supplied | 平均缺供电量（kWh/户） |

**计算维度**: 全局（statType=0）+ 分台区（statType=1, targetId=areaId）

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/index/reliability/**` | 指标数据 CRUD + 趋势图数据 |
| `/api/index/reliability/trend` | SAIDI/SAIFI 趋势折线 |
| `/api/index/reliability/rank` | 台区指标排名 |
| `/api/index/reliability/export` | 指标导出 Excel |
| `/api/index/alert/list` | 预警列表 |
| `/api/index/alert/handle` | 预警处理 |

---

### power-reliability-warning（隐患预警）
文件: 11 个 · 804 行

| 实体 | 表名 | 说明 |
|------|------|------|
| RiskPrediction | `risk_prediction` | 风险预测记录 |
| RiskWarningOrder | `risk_warning_order` | 隐患预警工单 |
| EquipmentDefect | `equipment_defect` | 设备缺陷 |

**多因子风险评估（4 因子）**:
1. **设备老化因子** — 运行年限 / 设计寿命
2. **历史故障因子** — 近 3 年故障次数
3. **负荷压力因子** — 当前负荷 / 额定容量
4. **环境风险因子** — 气象预警级别

评分 ≤ 3 → 低风险 / 4-6 → 中风险 / ≥ 7 → 高风险

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/warning/prediction/**` | 风险预测 CRUD + 列表/导出 |
| `/api/warning/order/**` | 预警工单 CRUD + 列表/导出 |

---

### power-reliability-review（复盘考核）
文件: 16 个 · 1,149 行

| 实体 | 表名 | 说明 |
|------|------|------|
| ReviewReport | `review_report` | 停电复盘报告 |
| RectificationTask | `rectification_task` | 整改任务 |
| PerformanceAssessment | `performance_assessment` | 绩效考核 |

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/review/report/**` | 复盘报告 CRUD + 导出 |
| `/api/review/rectification/**` | 整改任务 CRUD |
| `/api/review/assessment/**` | 绩效考核 CRUD + 排名 |

---

### power-reliability-dashboard（可视化大屏）
文件: 20 个 · 858 行

提供 REST 接口供前端 ECharts 大屏展示：总览卡片/指标趋势/台区排名/实时告警等。

---

### power-reliability-system-srv（系统权限）
文件: 60 个 · 1,516 行 · 文件数最多

| 实体 | 说明 |
|------|------|
| SysUser | 用户管理 |
| SysRole | 角色管理（RBAC）|
| SysMenu | 菜单/权限 |
| SysDept | 部门组织 |
| SysDict | 数据字典 |
| SysConfig | 系统配置 |
| SysOperationLog | 操作审计日志 |
| SysNotification | 系统通知 |

**API 端点**:
| 端点 | 说明 |
|------|------|
| `/api/system/auth/login` | 登录认证（JWT） |
| `/api/system/user/**` | 用户 CRUD |
| `/api/system/role/**` | 角色 CRUD + 分配菜单 |
| `/api/system/menu/**` | 菜单 CRUD |
| `/api/system/dept/**` | 部门 CRUD |
| `/api/system/config/**` | 配置管理 |
| `/api/system/log/**` | 操作日志查询 |
| `/api/system/dict/**` | 数据字典 |

---

### power-reliability-api（外部系统对接）
文件: 2 个 · 429 行

**数据同步接口**:
| 端点 | 来源系统 | 说明 |
|------|---------|------|
| `POST /api/external/sync/user` | 营销系统 | 同步用户档案 |
| `GET /api/external/sync/user/status` | 营销系统 | 查询同步状态 |
| `POST /api/external/sync/equipment` | PMS2.0 | 同步设备台账 |
| `POST /api/external/sync/area` | PMS2.0 | 同步台区档案 |
| `POST /api/external/sync/outage-event` | 调度系统 | 同步停电事件 |
| `POST /api/external/sync/repair-order` | 抢修平台 | 同步抢修工单 |
| `POST /api/external/sync/weather` | 气象系统 | 同步气象预警 |
| `GET /api/external/export/governance-ledger` | — | 导出治理台账 |
| `GET /api/external/export/reliability-index` | — | 导出可靠性指标 |

### power-reliability-notification（通知服务）
文件: 11 个 · 405 行

通知类型: alert（预警）/ task（任务）/ system（系统）/ approval（审批）  
消费频率: 每 5 分钟轮询未读通知

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行语言 |
| Spring Boot | 3.2.0 | 微服务基础框架 |
| Spring Cloud | 2023.0.0 | 微服务套件 |
| Spring Cloud Alibaba | 2022.0.0.0 | Alibaba 生态集成 |
| Spring Cloud Gateway | — | API 网关路由 |
| Nacos | — | 服务注册发现 + 配置中心 |
| Sentinel | 1.8.6 | 限流熔断 |
| MyBatis-Plus | 3.5.5 | ORM 持久层 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 6.0 | 缓存 |
| Knife4j | 4.3.0 | API 在线文档 |
| HuTool | 5.8.25 | Java 工具库 |
| Apache POI | — | Excel 导出 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 2.x | 前端框架 |
| Element UI | 2.12.0 | UI 组件库 |
| ECharts | 5.2.1 | 可视化图表 |
| Axios | 0.18.0 | HTTP 请求 |
| Webpack | 4.x | 构建工具 |

---

## 数据库概览

34 张业务表，按模块分类:

### 台账管理
| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `tr_area` | 台区档案 | area_code, area_name, substation_name, commissioning_date |
| `consumer` | 用户档案 | consumer_no, consumer_name, area_code, contact_phone |
| `equipment` | 设备台账 | equipment_code, area_id, commissioning_date, fault_count |
| `line` | 线路档案 | line_code, line_name, area_code, line_length |
| `ledger_validation_record` | 台账校验记录 | target_table, validation_type (完整性/逻辑性/规范性), result |

### 停电管控
| 表名 | 说明 |
|------|------|
| `outage_event` | 停电事件（主表，记录每次停电的起止/区域/影响用户） |
| `planned_outage` | 计划停电（含审批信息） |
| `fault_outage` | 故障停电（含故障原因/修复措施） |
| `outage_exemption` | 豁免停电（不计入频次统计） |
| `repair_order` | 抢修工单 |
| `outage_archive` | 停电归档 |

### 频繁停电治理
| 表名 | 说明 |
|------|------|
| `frequent_outage_ledger` | 治理台账（记录超标事件的治理全貌） |
| `governance_order` | 治理工单（含审批状态流转） |
| `governance_effect` | 治理成效（30/60/90天核验记录） |
| `outage_cause_analysis` | 归因分析（8类标准诱因分类） |

### 指标 & 预警
| 表名 | 说明 |
|------|------|
| `reliability_index` | 可靠性指标（SAIDI/SAIFI/CAIDI/ASAI） |
| `index_alert` | 指标异常预警 |
| `risk_prediction` | 风险预测（4因子评分） |
| `risk_warning_order` | 预警工单 |
| `equipment_defect` | 设备缺陷记录 |

### 复盘考核
| 表名 | 说明 |
|------|------|
| `review_report` | 停电复盘报告 |
| `rectification_task` | 整改任务 |
| `performance_assessment` | 绩效考核评分 |
| `report_template` | 标准化报表模板 |

### 系统权限
| 表名 | 说明 |
|------|------|
| `sys_user` | 系统用户 |
| `sys_role` | 角色定义 |
| `sys_role_menu` | 角色-菜单关联 |
| `sys_menu` | 菜单/权限树 |
| `sys_dept` | 部门组织 |
| `sys_dict` | 数据字典 |
| `sys_config` | 系统配置 |
| `sys_operation_log` | 操作审计日志 |
| `sys_notification` | 系统通知 |

---

## 定时任务

系统内置 6 个定时任务，统一在 `power-reliability-job` 模块管理：

| 任务 | 调度频率 | 功能 |
|------|---------|------|
| **IndexCalculateJob** | 每日 02:00 | 计算昨日 SAIDI/SAIFI/CAIDI/ASAI 指标（全局+分台区） |
| **OutageArchiveJob** | 每日 03:00 | 将已闭环的停电事件归档到 `outage_archive` 表 |
| **GovernanceEffectTrackJob** | 每日 04:00 | 已归档治理工单的 30/60/90 天成效核验 |
| **AlertCheckJob** | 每 30 分钟 | 检查指标阈值，超标的创建预警通知 |
| **GovernanceReviewJob** | 每 2 小时 | 自动扫描待审核的治理工单，超时自动升级 |
| **NotificationSendJob** | 每 5 分钟 | 消费未读通知记录（标记已读/扩展为真实发送） |

> **注意**: `power-reliability-job` 模块和各业务模块均有 `@EnableScheduling` 注解，确保定时任务能被 Spring 扫描执行。

---

## 自研引擎

### PolicyRuleEngine — 政策合规规则引擎

**位置**: `power-reliability-common.common.engine.PolicyRuleEngine`  
**用途**: 内置国家能源局供电可靠性新政阈值规则，所有判定全程留痕

**核心规则**:
| 规则 | 阈值 | 业务含义 |
|------|------|---------|
| `MAX_OUTAGE_PER_YEAR` | 5 次/年 | 单台区年停电不得超过 5 次 |
| `MAX_OUTAGE_PER_60_DAYS` | 3 次/60天 | 单台区 60 天内停电不得超过 3 次 |
| `SAIDI_THRESHOLD` | 300 分钟 | 台区 SAIDI 阈值 |
| `SAIFI_THRESHOLD` | 5 次/户 | 台区 SAIFI 阈值 |

**方法**:
- `checkYearlyCompliance(...)` — 年度频次合规检查
- `check60DayCompliance(...)` — 60 天频次合规检查
- `evaluateOverallRisk(...)` — 综合风险评估

---

### OutageCauseEngine — 停电智能归因引擎

**位置**: `power-reliability-common.common.engine.OutageCauseEngine`  
**用途**: 8 类标准诱因的多因子加权归因分析

**8 类标准诱因**:
| 编码 | 类别 | 典型场景 |
|------|------|---------|
| 1 | 设备老化缺陷 | 超期服役、绝缘老化 |
| 2 | 低压线路隐患 | 线路老化、接头松动 |
| 3 | 接头发热故障 | 接点氧化、接触电阻大 |
| 4 | 负荷过载 | 负荷超容、配变过载 |
| 5 | 三相不平衡 | 三相负荷分配不均 |
| 6 | 恶劣天气影响 | 雷雨、大风、冰雪 |
| 7 | 外力施工破坏 | 施工挖断、车辆撞杆 |
| 8 | 运维巡检不到位 | 消缺不及时、巡视漏项 |

**分析因子**:
| 因子 | 权重影响 |
|------|---------|
| 设备老化年限 | → 设备老化(70%) / 线路隐患(10%) |
| 负荷率 | → 负荷过载(60%) / 三相不平衡(30%) |
| 历史故障次数 | → 设备老化(25%) / 线路(25%) / 接头发热(20%) |
| 天气影响因子 | → 恶劣天气(80%) |

**输出**:
- 主要诱因（Top1 类别 + 置信度百分比）
- Top3 诱因排序（类别 + 得分百分比）
- 各因子得分明细

---

### WorkflowEngine — 全流程业务闭环引擎

**位置**: `power-reliability-common.common.engine.WorkflowEngine`  
**用途**: 7 节点审批流转模型，支持超时督办和自动升级

**节点定义**:
| 节点 | 名称 | 时限(小时) |
|------|------|-----------|
| 0 | 申报 | 24 |
| 1 | 班组审批 | 12 |
| 2 | 供电所审批 | 24 |
| 3 | 公司审批 | 48 |
| 4 | 现场执行 | 72 |
| 5 | 复电核验 | 24 |
| 6 | 归档完成 | 0 |

**核心方法**:
- `nextNode(int currentNode)` — 获取下一节点
- `isOverdue(WorkflowNode node, LocalDateTime startTime)` — 超时判断
- `escalate(WorkflowNode from, WorkflowNode to)` — 自动升级
- `getNodeConfig(int nodeCode)` — 节点配置查询

---

## API 接口总览

系统共计 **30 个 Controller**，按业务领域分组：

| 分组 | 前缀 | Controller 数量 |
|------|------|:--------------:|
| 台账管理 | `/api/ledger` | 5 |
| 停电管控 | `/api/outage` | 5 |
| 治理管理 | `/api/governance` | 3 |
| 指标核算 | `/api/index` | 2 |
| 预警管理 | `/api/warning` | 2 |
| 复盘考核 | `/api/review` | 3 |
| 外部对接 | `/api/external` | 1 |
| 系统权限 | `/api/system` | 7 |
| 通知服务 | `/api/notification` | 1 |
| 数据看板 | `/api/dashboard` | 1 |

每个 Controller 提供标准 RESTful 端点（`GET/POST/PUT/DELETE`），支持分页查询、多条件筛选、Excel 导出。

**统一返回格式**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

---

## 前端功能模块

12 个视图目录，44+ 个页面：

| 模块 | 页面数 | 页面 |
|------|:------:|------|
| `login` | 2 | 登录 / 注册 |
| `dashboard` | 1 | 数据大屏首页 |
| `ledger` | 4 | 台区管理 / 用户管理 / 设备台账 / 线路管理 |
| `outage` | 5 | 停电事件 / 计划停电 / 故障停电 / 豁免管理 / 抢修工单 |
| `governance` | 4 | 治理台账 / 治理工单 / 归因分析 / 成效跟踪 |
| `index-mgt` | 4 | 指标看板 / 趋势分析 / 台区排名 / 预警列表 |
| `warning` | 2 | 风险预测 / 预警工单 |
| `review` | 3 | 复盘报告 / 整改任务 / 绩效考核 |
| `system` | 6 | 用户管理 / 角色管理 / 菜单管理 / 部门管理 / 字典管理 / 日志查询 |
| `crm` | — | CRM 兼容预留 |
| `layout` | 13 | 布局组件/框架页面 |

---

## 快速开始

### 环境要求

| 工具 | 最低版本 |
|------|---------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Nacos | 2.x |

### 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < docs/sql/init-database.sql

# 2. 启动基础设施
#    - Nacos（服务注册中心 + 配置中心）
#    - Redis 6.0+
#    - MySQL 8.0+

# 3. 编译项目
mvn clean install -DskipTests

# 4. 按顺序启动微服务
#    推荐启动顺序:
#    a) power-reliability-gateway    (API 网关)
#    b) power-reliability-system-srv (系统权限/认证)
#    c) power-reliability-common     (已嵌入各模块)
#    d) 业务服务 (可并行启动)
#       - power-reliability-ledger
#       - power-reliability-outage
#       - power-reliability-governance
#       - power-reliability-index
#       - power-reliability-warning
#       - power-reliability-review
#       - power-reliability-dashboard
#       - power-reliability-notification
#       - power-reliability-job
#       - power-reliability-api
#
#    每个微服务启动命令:
java -jar power-reliability-xxx/target/power-reliability-xxx-1.0.0.jar

# 5. 启动前端
cd power-reliability-frontend
npm install          # 安装依赖（首次）
npm run dev          # 开发模式启动
# 默认访问: http://localhost:8099
```

### Docker 部署（推荐）

项目支持 Docker 容器化部署，各模块均已预置 `Dockerfile`。

```bash
# 构建镜像
mvn clean package -DskipTests
docker compose up -d
```

> **注意**: Nacos 需提前部署并配置相应的 Data ID。默认配置指向 `127.0.0.1:8848`。

---

## 配置参考

### 网关配置（power-reliability-gateway）

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes:
        - id: system-srv
          uri: lb://power-reliability-system-srv
          predicates:
            - Path=/api/system/**
          filters:
            - StripPrefix=1
        # ... 其余路由同理
```

### 认证白名单

网关中配置无需 Token 校验的路径：

```yaml
white-list:
  - /api/system/auth/login
  - /api/system/auth/register
```

### Sentinel 限流规则

文件: `power-reliability-gateway/src/main/resources/sentinel-gateway-rules.json`

```json
[
  { "resource": "/api/**",       "count": 1000, "grade": 1 },
  { "resource": "/api/dashboard/**", "count": 500,  "grade": 1 },
  { "resource": "/api/ledger/**",    "count": 200,  "grade": 1 },
  { "resource": "/api/outage/**",    "count": 200,  "grade": 1 },
  { "resource": "/api/governance/**","count": 200,  "grade": 1 }
]
```

### 业务服务配置（示例）

```yaml
server:
  port: 8099   # 各模块端口不同

spring:
  application:
    name: power-reliability-xxx
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

---

## 开发指南

### 代码规范

- 包路径: `com.powerreliability.{module}.{layer}`
- Controller 统一加 `@Tag(name="...")` Swagger 描述
- Service 接口命名: `I{Entity}Service` / 实现: `{Entity}ServiceImpl`
- Mapper 继承 `BaseMapper<T>`，使用 MyBatis-Plus Lambda 查询
- 返回统一使用 `Result.success()` / `Result.error()`

### 新增一个业务模块

```bash
# 1. 创建模块目录
mkdir power-reliability-xxx
mkdir -p power-reliability-xxx/src/main/java/com/powerreliability/xxx/{controller,entity,mapper,service}
mkdir -p power-reliability-xxx/src/main/resources

# 2. 添加 pom.xml（参考其他模块）
# 3. 在 parent pom.xml 中添加 module
# 4. 添加 Application.java + application.yml
# 5. 在网关添加路由规则
```

### 数据库变更

```bash
# 在 docs/sql/init-database.sql 末尾追加 DDL
# 或创建增量脚本 docs/sql/upgrade/YYYYMMDD.sql
```

---

## 运维要点

### 日志

- 各模块日志级别默认: `com.powerreliability: info`
- 控制台日志 + 文件输出（Spring Boot 默认配置）
- 操作审计日志自动记录到 `sys_operation_log` 表

### 监控

- **服务健康**: Spring Boot Actuator `/actuator/health`
- **限流监控**: Sentinel Dashboard（需另行部署）
- **指标监控**: Prometheus + Grafana（可选集成）

### 数据备份

```bash
# 全量备份
mysqldump -u root -p power_reliability > backup/$(date +%Y%m%d).sql

# 增量备份方式
mysqlbinlog mysql-bin.000001 > backup/binlog.sql
```

### 扩容

- 各微服务无状态，支持水平扩展
- 网关自动负载均衡（Nacos + Ribbon）
- 前端可部署 CDN 或 Nginx 反向代理

---

## 设计文档

详见 `docs/` 目录：

- `docs/sql/init-database.sql` — 数据库初始化脚本（34 张表 DDL + 预置数据）
- `docs/design/设计方案.md` — 原始设计方案文档（368 行覆盖 8 大模块）

---

## 许可证

Copyright © 2026
