package com.powerreliability.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 整改任务
 */
@Data
@TableName("rectification_task")
public class RectificationTask {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务编号 */
    private String taskNo;

    /** 关联复盘报告ID */
    private Long reportId;

    /** 关联停电事件ID */
    private Long outageEventId;

    /** 问题描述 */
    private String problemDesc;

    /** 整改措施 */
    private String rectificationMeasure;

    /** 整改类型：1-设备更换 2-线路改造 3-运维加强 4-管理优化 */
    private Integer rectificationType;

    /** 责任部门 */
    private String responsibleDept;

    /** 责任人 */
    private String responsiblePerson;

    /** 整改期限 */
    private LocalDate deadline;

    /** 任务状态：0-待整改 1-整改中 2-已完成 3-已验收 */
    private Integer taskStatus;

    /** 完成描述 */
    private String completeDesc;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 验收结果：0-未验收 1-通过 2-不通过 */
    private Integer acceptResult;

    /** 验收意见 */
    private String acceptOpinion;

    /** 验收人 */
    private String acceptPerson;

    /** 验收时间 */
    private LocalDateTime acceptTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
