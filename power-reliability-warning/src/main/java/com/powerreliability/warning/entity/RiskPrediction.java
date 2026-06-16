package com.powerreliability.warning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 隐患预判记录
 */
@Data
@TableName("risk_prediction")
public class RiskPrediction {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联设备ID */
    private Long equipmentId;

    /** 关联线路ID */
    private Long lineId;

    /** 关联台区ID */
    private Long areaId;

    /** 预判类型：1-设备老化 2-负荷过载 3-外力破坏 4-天气影响 5-综合 */
    private Integer predictionType;

    /** 风险等级：1-低 2-中 3-高 4-紧急 */
    private Integer riskLevel;

    /** 风险评分（0-100） */
    private BigDecimal riskScore;

    /** 设备运行年限 */
    private Integer equipmentAge;

    /** 历史故障次数 */
    private Integer historicalFaultCount;

    /** 近期负荷率（%） */
    private BigDecimal loadRate;

    /** 天气影响因子 */
    private BigDecimal weatherFactor;

    /** 隐患描述 */
    private String predictionDesc;

    /** 预判结果详情（JSON） */
    private String predictionDetail;

    /** 建议措施 */
    private String suggestion;

    /** 预判时间 */
    private LocalDateTime predictionTime;

    /** 状态：0-待处理 1-已生成工单 2-已忽略 */
    private Integer status;

    /** 处理人 */
    private String handler;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
