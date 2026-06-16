package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.powerreliability.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outage_event")
public class OutageEvent {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Excel(name = "事件编号", sort = 1)
    private String eventNo;

    @Excel(name = "停电类型", sort = 2)
    private Integer outageType;

    @Excel(name = "停电开始时间", sort = 3, width = 20)
    private LocalDateTime outageStartTime;

    @Excel(name = "复电时间", sort = 4, width = 20)
    private LocalDateTime outageEndTime;

    @Excel(name = "停电时长(分钟)", sort = 5)
    private Integer outageDuration;

    private Long areaId;

    @Excel(name = "台区编码", sort = 6)
    private String areaCode;

    @Excel(name = "台区名称", sort = 7)
    private String areaName;

    @Excel(name = "影响用户数", sort = 8)
    private Integer affectedConsumerCount;

    private String affectedConsumerIds;

    @Excel(name = "故障类型", sort = 9)
    private String faultType;

    @Excel(name = "故障原因", sort = 10, width = 40)
    private String faultReason;

    @Excel(name = "故障位置", sort = 11, width = 30)
    private String faultLocation;

    @Excel(name = "是否豁免", sort = 12)
    private Integer isExempt;

    private Integer exemptType;

    private String exemptBasis;

    @Excel(name = "是否闭环", sort = 13)
    private Integer isClosed;

    @Excel(name = "闭环时间", sort = 14, width = 20)
    private LocalDateTime closeTime;

    @Excel(name = "备注", sort = 15, width = 30)
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
