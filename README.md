# 低压供电可靠性全流程管控系统

> 基于 Spring Cloud Alibaba 微服务架构，聚焦 0.4kV 低压配网供电可靠性全流程管控，全面适配 2025 年国家发改委、能源局供电可靠性新政要求。

## 核心能力

- 📋 **基础台账管理** — 台区/用户/设备/线路全生命周期标准化台账
- ⚡ **停电事件全流程管控** — 计划停电/故障停电/豁免停电闭环管理
- 🔄 **频繁停电智能治理** — 智能筛查/归因分析/工单闭环/成效核验
- 📊 **可靠性指标核算** — SAIDI/SAIFI 自动统计、趋势分析、异常预警
- ⚠️ **隐患预判与预警** — 多因子风险评估、主动消缺闭环
- 📝 **复盘整改与绩效考核** — 停电复盘、问题整改、绩效联动
- 📈 **可视化大屏** — 全域态势一屏总览、数据钻取
- 🔐 **权限与运维管理** — RBAC 权限、日志审计、数据安全

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 微服务框架 | Spring Cloud Alibaba | 服务注册/配置/网关/熔断 |
| 基础框架 | Spring Boot 3.2 | 快速构建微服务 |
| 持久层 | MyBatis-Plus 3.5 | ORM 框架 |
| 数据库 | MySQL 8.0 + Redis 6.0 | 关系库 + 缓存 |
| API 文档 | Knife4j | 在线接口文档 |
| 前端 | Vue 2 + Element UI | PC 管理端 |
| 可视化 | ECharts | 大屏展示 |
| 部署 | Docker | 容器化部署 |

## 项目结构

```
power-reliability-system/
├── power-reliability-common/     # 公共模块（实体/工具/配置）
├── power-reliability-gateway/    # API 网关
├── power-reliability-ledger/     # 基础台账管理服务
├── power-reliability-outage/     # 停电事件管控服务
├── power-reliability-governance/ # 频繁停电治理服务
├── power-reliability-index/      # 可靠性指标核算服务
├── power-reliability-warning/    # 隐患预判预警服务
├── power-reliability-review/     # 复盘考核管理服务
├── power-reliability-dashboard/  # 可视化大屏服务
├── power-reliability-system-srv/ # 系统权限运维服务
├── power-reliability-api/        # API 接口定义
└── docs/                         # 文档
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+
- Node.js 18+（前端）

### 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p < docs/sql/init-database.sql

# 2. 启动基础设施（Nacos、Redis、MySQL）
# 3. 按顺序启动微服务（Gateway → System → 业务服务）
# 4. 启动前端
cd power-reliability-frontend
npm install
npm run dev
```

## 许可证

Copyright © 2026
