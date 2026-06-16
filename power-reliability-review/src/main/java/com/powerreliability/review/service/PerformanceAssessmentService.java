package com.powerreliability.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.review.entity.PerformanceAssessment;

/**
 * 绩效考核服务
 */
public interface PerformanceAssessmentService extends IService<PerformanceAssessment> {

    /**
     * 自动计算绩效评分
     * 基于供电可靠率、故障处理及时率、隐患整治率等多维度自动评分
     * @param id 考核记录ID（如传入null则对所有未计算记录进行计算）
     * @return 计算完成的记录数
     */
    int calculate(Long id);
}
