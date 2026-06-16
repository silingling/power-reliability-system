package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("performance_assessment")
public class DashboardPerformance {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer assessmentPeriod;

    private Integer assessmentYear;

    private Integer assessmentMonth;

    private Integer assessmentTargetType;

    private Long assessmentTargetId;

    private String assessmentTargetName;

    private BigDecimal areaViolationRate;

    private BigDecimal governanceClosureRate;

    private BigDecimal warningDisposalRate;

    private BigDecimal avgRepairTime;

    private BigDecimal outageComplianceRate;

    private BigDecimal totalScore;

    private String assessmentGrade;

    @TableLogic
    private Integer isDeleted;
}
