package com.powerreliability.outage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("planned_outage")
public class PlannedOutage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private String eventNo;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String outageContent;

    private String constructionTeam;

    private String responsiblePerson;

    private String outageScope;

    private String constructionPlan;

    private Integer approvalStatus;

    private Integer approvalLevel;

    private String approvalRecord;

    private Integer complianceCheck;

    private String complianceDetail;

    private Integer isOvertime;

    private Integer overtimeMinutes;

    private Integer isOverScope;

    private Integer executionStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
