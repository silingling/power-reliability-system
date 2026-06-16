package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("frequent_outage_ledger")
public class DashboardFrequentOutageLedger {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private Integer violationType;

    private Integer currentYearCount;

    private Integer last60dCount;

    private Integer riskLevel;

    private String mainCause;

    private Integer governanceStatus;

    private LocalDate governanceStartDate;

    private LocalDate governanceEndDate;

    @TableLogic
    private Integer isDeleted;
}
