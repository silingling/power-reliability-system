package com.powerreliability.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 绩效考核
 */
@Data
@TableName("performance_assessment")
public class PerformanceAssessment {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考核编号 */
    private String assessmentNo;

    /** 考核名称 */
    private String assessmentName;

    /** 考核周期：1-月度 2-季度 3-年度 */
    private Integer assessmentPeriod;

    /** 考核年份 */
    private Integer assessmentYear;

    /** 考核月份/季度 */
    private Integer assessmentMonth;

    /** 考核对象ID（部门/班组） */
    private String targetId;

    /** 考核对象名称 */
    private String targetName;

    /** 总得分（0-100） */
    private BigDecimal totalScore;

    /** 供电可靠率得分 */
    private BigDecimal reliabilityScore;

    /** 故障处理及时率得分 */
    private BigDecimal timelyRateScore;

    /** 隐患整治率得分 */
    private BigDecimal rectificationRateScore;

    /** 用户投诉率得分 */
    private BigDecimal complaintRateScore;

    /** 停电复电效率得分 */
    private BigDecimal restoreEfficiencyScore;

    /** 报告完成率得分 */
    private BigDecimal reportCompletionScore;

    /** 考核等级：A-优秀 B-良好 C-合格 D-不合格 */
    private String assessmentGrade;

    /** 考核意见 */
    private String assessmentOpinion;

    /** 考核状态：0-未计算 1-已计算 2-已发布 */
    private Integer assessmentStatus;

    /** 计算时间 */
    private LocalDateTime calcTime;

    /** 备注 */
    private String remarks;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
