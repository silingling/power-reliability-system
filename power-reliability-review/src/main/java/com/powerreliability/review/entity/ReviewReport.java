package com.powerreliability.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停电复盘报告
 */
@Data
@TableName("review_report")
public class ReviewReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报告编号 */
    private String reportNo;

    /** 报告标题 */
    private String reportTitle;

    /** 关联停电事件ID */
    private Long outageEventId;

    /** 停电事件编号 */
    private String outageEventNo;

    /** 停电发生时间 */
    private LocalDateTime outageTime;

    /** 复电时间 */
    private LocalDateTime restoreTime;

    /** 停电时长（分钟） */
    private Integer outageDuration;

    /** 停电类型：1-计划停电 2-故障停电 3-临时停电 */
    private Integer outageType;

    /** 影响用户数 */
    private Integer affectedConsumerCount;

    /** 停电原因 */
    private String outageReason;

    /** 停电经过 */
    private String outageProcess;

    /** 原因分析 */
    private String causeAnalysis;

    /** 暴露问题 */
    private String identifiedProblems;

    /** 整改建议 */
    private String rectificationSuggestion;

    /** 是否豁免：0-否 1-是 */
    private Integer isExempt;

    /** 豁免类型 */
    private Integer exemptType;

    /** 责任单位 */
    private String responsibleUnit;

    /** 报告状态：0-草稿 1-已发布 */
    private Integer reportStatus;

    /** 报告人 */
    private String reportPerson;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
