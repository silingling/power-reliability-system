package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tr_area")
public class TrArea {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String areaCode;

    private String areaName;

    private String substationName;

    private String powerSupplyStation;

    private String maintenanceTeam;

    private String responsiblePerson;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String powerSupplyScope;

    private LocalDate commissioningDate;

    private BigDecimal designCapacity;

    private BigDecimal ratedLoad;

    private BigDecimal currentLoad;

    private BigDecimal supplyRadius;

    private String transformerModel;

    private String transformerManufacturer;

    private Integer transformerYears;

    private Integer totalConsumers;

    private Integer residentialCount;

    private Integer commercialCount;

    private Integer areaStatus;

    private LocalDate lastOverhaulDate;

    private Integer overhaulCount;

    private String defectInfo;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
