package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("outage_exemption")
public class OutageExemption {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private String eventNo;

    private Integer exemptType;

    private String exemptReason;

    private String exemptBasis;

    private String weatherData;

    private String damageEvidence;

    private String userFaultProof;

    private String verdictPerson;

    private LocalDateTime verdictTime;

    private Integer verdictStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
