package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String equipmentCode;

    private String equipmentName;

    private Integer equipmentType;

    private String equipmentModel;

    private String specification;

    private String manufacturer;

    private LocalDate manufactureDate;

    private LocalDate commissioningDate;

    private LocalDate lastInspectionDate;

    private Integer inspectionCycle;

    private Long areaId;

    private String areaCode;

    private String lineCode;

    private String locationDesc;

    private String maintenancePerson;

    private String defectInfo;

    private Integer faultCount;

    private Integer replacementCount;

    private Integer equipmentStatus;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
