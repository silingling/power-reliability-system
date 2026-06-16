package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("risk_warning_order")
public class DashboardRiskWarning {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private String areaCode;

    private Integer riskType;

    private Integer riskLevel;

    private String warningDesc;

    private LocalDateTime warningTime;

    private String pushChannel;

    private String receiver;

    private LocalDateTime responseDeadline;

    private Integer orderStatus;

    @TableLogic
    private Integer isDeleted;
}
