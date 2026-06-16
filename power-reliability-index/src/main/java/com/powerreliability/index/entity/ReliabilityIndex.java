package com.powerreliability.index.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.powerreliability.common.annotation.Excel;
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
    @Excel(name = "统计类型", sort = 1)
    private Long statType;

    /** 目标ID */
    @Excel(name = "目标ID", sort = 2)
    private Long targetId;

    /** 统计周期 0-日, 1-月, 2-年 */
    @Excel(name = "统计周期", sort = 3)
    private Integer period;

    /** 统计日期 */
    @Excel(name = "统计日期", sort = 4)
    private LocalDate statDate;

    /** 平均供电可用率（ASAI） */
    @Excel(name = "ASAI(%)", sort = 5)
    private Double asai;

    /** 平均供电不可用率（ASIDI） */
    @Excel(name = "ASIDI(%)", sort = 6)
    private Double asidi;

    /** 系统平均停电频率（SAIFI） */
    @Excel(name = "SAIFI(次/户)", sort = 7)
    private Double saifi;

    /** 系统平均停电持续时间（SAIDI） */
    @Excel(name = "SAIDI(分钟)", sort = 8)
    private Double saidi;

    /** 用户平均停电持续时间（CAIDI） */
    @Excel(name = "CAIDI(分钟)", sort = 9)
    private Double caidi;

    /** 总缺供电量（ENS）kWh */
    @Excel(name = "ENS(kWh)", sort = 10)
    private Double ens;

    /** 平均缺供电量（AENS）kWh/户 */
    @Excel(name = "AENS(kWh/户)", sort = 11)
    private Double aens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
