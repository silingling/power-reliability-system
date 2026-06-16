package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.powerreliability.common.annotation.Excel;
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
    @Excel(name = "工单编号", sort = 1)
    private String orderNo;

    /** 关联事件ID */
    @Excel(name = "关联事件ID", sort = 2)
    private String eventId;

    /** 工单标题 */
    @Excel(name = "工单标题", sort = 3, width = 30)
    private String title;

    /** 工单内容 */
    @Excel(name = "工单内容", sort = 4, width = 40)
    private String content;

    /** 负责单位 */
    @Excel(name = "负责单位", sort = 5)
    private String responsibleUnit;

    /** 负责人 */
    @Excel(name = "负责人", sort = 6)
    private String responsiblePerson;

    /** 状态: 0-待分配, 1-待审核, 2-已通过, 3-已驳回 */
    @Excel(name = "工单状态", sort = 7)
    private Integer status;

    /** 审核结果: 0-未审核, 1-通过, 2-驳回 */
    @Excel(name = "审核结果", sort = 8)
    private Integer reviewResult;

    /** 截止日期 */
    @Excel(name = "截止日期", sort = 9)
    private LocalDate deadline;

    /** 分配时间 */
    @Excel(name = "分配时间", sort = 10, width = 20)
    private LocalDateTime dispatchTime;

    /** 提交时间 */
    @Excel(name = "提交时间", sort = 11, width = 20)
    private LocalDateTime submitTime;

    /** 审核时间 */
    @Excel(name = "审核时间", sort = 12, width = 20)
    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
