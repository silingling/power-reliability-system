package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long eventId;

    private String eventNo;

    private LocalDateTime dispatchTime;

    private LocalDateTime arriveTime;

    private String repairTeam;

    private String repairPersons;

    private String faultFinding;

    private String repairMeasures;

    private LocalDateTime repairCompleteTime;

    private LocalDateTime recoveryVerifyTime;

    private String verifyPerson;

    private Integer verifyResult;

    private String faultEvidence;

    private Integer orderStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
