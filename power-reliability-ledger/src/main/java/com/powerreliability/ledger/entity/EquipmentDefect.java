package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_defect")
public class EquipmentDefect {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long equipmentId;

    private String equipmentCode;

    private Integer defectType;

    private String defectDesc;

    private LocalDateTime discoveryDate;

    private String discoveryPerson;

    private Integer repairStatus;

    private LocalDateTime repairDate;

    private String repairPerson;

    private String repairMeasures;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
