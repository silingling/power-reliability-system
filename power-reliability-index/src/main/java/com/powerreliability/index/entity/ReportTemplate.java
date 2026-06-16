package com.powerreliability.index.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表模板配置表
 */
@Data
@TableName("report_template")
public class ReportTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板编码: MONTHLY / QUARTERLY / ANNUAL */
    private String templateCode;

    /** 模板名称: 月度可靠性报表 / 季度可靠性报表 / 年度可靠性报表 */
    private String templateName;

    /** 模板描述 */
    private String description;

    /** 模板配置（JSON格式） */
    private String templateConfig;

    /** 是否默认模板: 0-否 1-是 */
    private Integer isDefault;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
