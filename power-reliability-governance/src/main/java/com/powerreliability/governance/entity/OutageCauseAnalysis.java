package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停电归因分析
 */
@Data
@TableName("outage_cause_analysis")
public class OutageCauseAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联事件ID */
    private String eventId;

    /** 事件名称 */
    private String eventName;

    /** 分析结论 */
    private String analysisResult;

    /** 直接原因 */
    private String directCause;

    /** 根本原因 */
    private String rootCause;

    /** 责任单位 */
    private String responsibilityUnit;

    /** 分析时间 */
    private LocalDateTime analysisTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
