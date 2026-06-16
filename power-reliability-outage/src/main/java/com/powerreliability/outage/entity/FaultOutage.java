package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fault_outage")
public class FaultOutage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private String eventNo;

    private Integer faultSource;

    private String sourceDetail;

    private LocalDateTime faultTime;

    private String faultType;

    private Integer faultLevel;

    private String tripDevice;

    private String protectionAction;

    private LocalDateTime recoveryTime;

    private String recoveryMethod;

    private String dataSource;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
