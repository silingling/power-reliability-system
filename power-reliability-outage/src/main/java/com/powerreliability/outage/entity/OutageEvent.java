package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outage_event")
public class OutageEvent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventNo;

    private Integer outageType;

    private LocalDateTime outageStartTime;

    private LocalDateTime outageEndTime;

    private Integer outageDuration;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private Integer affectedConsumerCount;

    private String affectedConsumerIds;

    private String faultType;

    private String faultReason;

    private String faultLocation;

    private Integer isExempt;

    private Integer exemptType;

    private String exemptBasis;

    private Integer isClosed;

    private LocalDateTime closeTime;

    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
