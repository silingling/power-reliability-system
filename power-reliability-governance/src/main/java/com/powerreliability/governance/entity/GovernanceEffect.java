package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 治理成效跟踪
 */
@Data
@TableName("governance_effect")
public class GovernanceEffect {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联工单ID */
    private Long orderId;

    /** 成效描述 */
    private String effectDescription;

    /** 停电减少率 */
    private BigDecimal outageReductionRate;

    /** 影响用户减少数 */
    private Integer affectedUserReduction;

    /** 治理前情况 */
    private String beforeMeasure;

    /** 治理后情况 */
    private String afterMeasure;

    /** 成效评分 */
    private Integer effectScore;

    /** 成效统计时间 */
    private LocalDate effectTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
