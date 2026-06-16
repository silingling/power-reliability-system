package com.powerreliability.ledger.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 台账合规校验记录实体
 */
@Data
@TableName("ledger_validation_record")
public class LedgerValidationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标表名（如 tr_area / consumer / equipment / line） */
    private String targetTable;

    /** 目标记录ID */
    private Long targetId;

    /** 校验类型：COMPLETENESS / LOGICAL / STANDARD */
    private String validationType;

    /** 字段名 */
    private String fieldName;

    /** 字段当前值（字符串表示） */
    private String fieldValue;

    /** 问题描述 */
    private String issueDesc;

    /** 严重级别：ERROR / WARNING */
    private String severityLevel;

    /** 修复状态：0-未修复 1-已修复 */
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
