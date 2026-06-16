package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("line")
public class Line {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String lineCode;

    private String lineName;

    private Integer lineType;

    private String voltageLevel;

    private Long areaId;

    private String areaCode;

    private String startPoint;

    private String endPoint;

    private BigDecimal totalLength;

    private String lineModel;

    private LocalDate commissioningDate;

    private String maintenancePerson;

    private String defectInfo;

    private Integer faultCount;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
