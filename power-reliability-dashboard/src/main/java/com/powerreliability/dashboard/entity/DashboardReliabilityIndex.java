package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reliability_index")
public class DashboardReliabilityIndex {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer statType;

    private String statTargetName;

    private Integer statPeriod;

    private LocalDate statStartDate;

    private LocalDate statEndDate;

    private BigDecimal saidi;

    private BigDecimal saifi;

    private BigDecimal avgOutageDuration;

    private BigDecimal faultRecoveryRate;

    private BigDecimal avgFaultRecoveryTime;

    private BigDecimal plannedComplianceRate;

    private BigDecimal areaViolationRate;

    private BigDecimal frequentClearRate;

    private Integer totalOutageCount;

    private Integer plannedOutageCount;

    private Integer faultOutageCount;

    private Integer exemptOutageCount;

    private Integer totalAffectedConsumers;

    private Integer totalOutageMinutes;

    @TableLogic
    private Integer isDeleted;
}
