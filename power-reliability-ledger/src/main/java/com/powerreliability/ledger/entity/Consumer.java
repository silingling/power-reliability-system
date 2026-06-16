package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("consumer")
public class Consumer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String consumerNo;

    private String consumerName;

    private String consumerAddress;

    private Integer consumerType;

    private Long areaId;

    private String areaCode;

    private String lineCode;

    private String contactPhone;

    private String ownershipBoundary;

    private LocalDate openDate;

    private LocalDate closeDate;

    private String loadCharacteristic;

    private Integer status;

    private Integer outageCountYear;

    private Integer outageCount60d;

    private Integer complaintCount;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
