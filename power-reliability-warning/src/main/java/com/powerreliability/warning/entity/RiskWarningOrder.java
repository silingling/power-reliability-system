package com.powerreliability.warning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐患预警工单
 */
@Data
@TableName("risk_warning_order")
public class RiskWarningOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号 */
    private String orderNo;

    /** 关联预判记录ID */
    private Long predictionId;

    /** 预警类型：1-设备预警 2-线路预警 3-台区预警 4-综合预警 */
    private Integer warningType;

    /** 紧急程度：1-一般 2-重要 3-紧急 */
    private Integer urgencyLevel;

    /** 预警标题 */
    private String warningTitle;

    /** 预警内容 */
    private String warningContent;

    /** 关联设备ID */
    private Long equipmentId;

    /** 关联线路ID */
    private Long lineId;

    /** 关联台区ID */
    private Long areaId;

    /** 下发对象 */
    private String dispatchTarget;

    /** 下发时间 */
    private LocalDateTime dispatchTime;

    /** 责任人 */
    private String responsiblePerson;

    /** 处置状态：0-待处置 1-处置中 2-已处置 3-已复核 */
    private Integer disposeStatus;

    /** 处置描述 */
    private String disposeDesc;

    /** 处置人 */
    private String disposePerson;

    /** 处置时间 */
    private LocalDateTime disposeTime;

    /** 复核结果：0-未复核 1-通过 2-不通过 */
    private Integer reviewResult;

    /** 复核意见 */
    private String reviewOpinion;

    /** 复核人 */
    private String reviewPerson;

    /** 复核时间 */
    private LocalDateTime reviewTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
