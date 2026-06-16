package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 频繁停电台账
 */
@Data
@TableName("frequent_outage_ledger")
public class FrequentOutageLedger {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 停电事件ID */
    private String eventId;

    /** 区域编码 */
    private String areaCode;

    /** 区域名称 */
    private String areaName;

    /** 停电开始时间 */
    private LocalDateTime outageStartTime;

    /** 停电结束时间 */
    private LocalDateTime outageEndTime;

    /** 停电时长（分钟） */
    private Integer outageDuration;

    /** 影响用户数 */
    private Integer affectedUsers;

    /** 风险等级 high/medium/low */
    private String riskLevel;

    /** 治理状态 pending/processing/completed */
    private String governanceStatus;

    /** 原因分类 */
    private String causeCategory;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
