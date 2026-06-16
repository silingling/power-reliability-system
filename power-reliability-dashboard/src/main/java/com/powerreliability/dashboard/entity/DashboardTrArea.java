package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tr_area")
public class DashboardTrArea {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String areaCode;

    private String areaName;

    private String powerSupplyStation;

    private String maintenanceTeam;

    private String responsiblePerson;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer areaStatus;

    private Integer totalConsumers;

    @TableLogic
    private Integer isDeleted;
}
