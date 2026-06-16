package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outage_event")
public class DashboardOutageEvent {
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

    private Integer isExempt;

    private Integer isClosed;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
}
