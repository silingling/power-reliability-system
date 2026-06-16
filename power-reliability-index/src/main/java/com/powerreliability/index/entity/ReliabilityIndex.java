package com.powerreliability.index.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 可靠性指标统计
 */
@Data
@TableName("reliability_index")
public class ReliabilityIndex {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计类型 */
    private Long statType;

    /** 目标ID */
    private Long targetId;

    /** 统计周期 0-日, 1-月, 2-年 */
    private Integer period;

    /** 统计日期 */
    private LocalDate statDate;

    /** 平均供电可用率（ASAI） */
    private Double asai;

    /** 平均供电不可用率（ASIDI） */
    private Double asidi;

    /** 系统平均停电频率（SAIFI） */
    private Double saifi;

    /** 系统平均停电持续时间（SAIDI） */
    private Double saidi;

    /** 用户平均停电持续时间（CAIDI） */
    private Double caidi;

    /** 总缺供电量（ENS）kWh */
    private Double ens;

    /** 平均缺供电量（AENS）kWh/户 */
    private Double aens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
