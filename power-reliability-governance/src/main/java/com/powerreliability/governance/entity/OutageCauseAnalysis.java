package com.powerreliability.governance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.powerreliability.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 停电归因分析（八大标准诱因分类）
 */
@Data
@TableName("outage_cause_analysis")
public class OutageCauseAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联事件ID */
    @Excel(name = "关联事件ID", sort = 1)
    private String eventId;

    /** 事件名称 */
    @Excel(name = "事件名称", sort = 2, width = 30)
    private String eventName;

    /**
     * 八大标准诱因分类：
     * 1-设备老化 2-线路隐患 3-接头发热 4-负荷过载
     * 5-三相不平衡 6-天气影响 7-外力破坏 8-运维不到位
     */
    @Excel(name = "诱因分类", sort = 3, dict = "outage_cause_type")
    private Integer causeType;

    /** 直接原因 */
    @Excel(name = "直接原因", sort = 4, width = 40)
    private String directCause;

    /** 根本原因 */
    @Excel(name = "根本原因", sort = 5, width = 40)
    private String rootCause;

    /** 分析结论 */
    @Excel(name = "分析结论", sort = 6, width = 50)
    private String analysisResult;

    /** 责任单位 */
    @Excel(name = "责任单位", sort = 7)
    private String responsibilityUnit;

    /** 分析人 */
    @Excel(name = "分析人", sort = 8)
    private String analyst;

    /** 分析时间 */
    @Excel(name = "分析时间", sort = 9, width = 20)
    private LocalDateTime analysisTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
