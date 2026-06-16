package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("governance_order")
public class DashboardGovernanceOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private String problemDesc;

    private String responsibleTeam;

    private String responsiblePerson;

    private LocalDate deadlineDate;

    private Integer orderStatus;

    @TableLogic
    private Integer isDeleted;
}
