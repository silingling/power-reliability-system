package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 治理工单
 */
@Data
@TableName("governance_order")
public class GovernanceOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号 */
    private String orderNo;

    /** 关联事件ID */
    private String eventId;

    /** 工单标题 */
    private String title;

    /** 工单内容 */
    private String content;

    /** 负责单位 */
    private String responsibleUnit;

    /** 负责人 */
    private String responsiblePerson;

    /** 状态: 0-待分配, 1-待审核, 2-已通过, 3-已驳回 */
    private Integer status;

    /** 审核结果: 0-未审核, 1-通过, 2-驳回 */
    private Integer reviewResult;

    /** 截止日期 */
    private LocalDate deadline;

    /** 分配时间 */
    private LocalDateTime dispatchTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
