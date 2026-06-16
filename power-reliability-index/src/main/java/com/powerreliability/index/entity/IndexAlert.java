package com.powerreliability.index.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指标异常预警
 */
@Data
@TableName("index_alert")
public class IndexAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联指标ID */
    private Long indexId;

    /** 统计类型 */
    private Long statType;

    /** 目标ID */
    private Long targetId;

    /** 预警类型 */
    private String alertType;

    /** 预警等级 */
    private String alertLevel;

    /** 指标名称 */
    private String indexName;

    /** 指标值 */
    private Double indexValue;

    /** 阈值 */
    private Double thresholdValue;

    /** 偏差率 */
    private Double deviationRate;

    /** 预警时间 */
    private LocalDateTime alertTime;

    /** 状态: 0-未处理, 1-已处理 */
    private Integer status;

    /** 处理措施 */
    private String handleMeasures;

    /** 处理时间 */
    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
