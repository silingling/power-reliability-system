package com.powerreliability.review.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.review.entity.PerformanceAssessment;
import com.powerreliability.review.mapper.PerformanceAssessmentMapper;
import com.powerreliability.review.service.PerformanceAssessmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PerformanceAssessmentServiceImpl extends ServiceImpl<PerformanceAssessmentMapper, PerformanceAssessment>
        implements PerformanceAssessmentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int calculate(Long id) {
        log.info("开始计算绩效评分...");

        List<PerformanceAssessment> assessments;

        if (id != null) {
            PerformanceAssessment assessment = getById(id);
            if (assessment == null) {
                log.warn("考核记录不存在: {}", id);
                return 0;
            }
            assessments = List.of(assessment);
        } else {
            assessments = lambdaQuery()
                    .eq(PerformanceAssessment::getAssessmentStatus, 0) // 未计算
                    .list();
        }

        if (assessments.isEmpty()) {
            log.info("没有待计算的考核记录");
            return 0;
        }

        for (PerformanceAssessment assessment : assessments) {
            calculateOne(assessment);
            updateById(assessment);
        }

        log.info("绩效评分计算完成，共 {} 条", assessments.size());
        return assessments.size();
    }

    // ==================== 评分算法 ====================

    private void calculateOne(PerformanceAssessment assessment) {
        // ========== 实际项目中各维度数据应从相应模块获取，此处模拟 ==========

        // 1. 供电可靠率得分 (RS-1) - 满分25
        BigDecimal reliabilityScore = computeReliabilityScore();
        assessment.setReliabilityScore(reliabilityScore);

        // 2. 故障处理及时率得分 - 满分20
        BigDecimal timelyRateScore = computeTimelyRateScore();
        assessment.setTimelyRateScore(timelyRateScore);

        // 3. 隐患整治率得分 - 满分20
        BigDecimal rectificationRateScore = computeRectificationRateScore();
        assessment.setRectificationRateScore(rectificationRateScore);

        // 4. 用户投诉率得分 - 满分15
        BigDecimal complaintRateScore = computeComplaintRateScore();
        assessment.setComplaintRateScore(complaintRateScore);

        // 5. 停电复电效率得分 - 满分10
        BigDecimal restoreEfficiencyScore = computeRestoreEfficiencyScore();
        assessment.setRestoreEfficiencyScore(restoreEfficiencyScore);

        // 6. 报告完成率得分 - 满分10
        BigDecimal reportCompletionScore = computeReportCompletionScore();
        assessment.setReportCompletionScore(reportCompletionScore);

        // 总分
        BigDecimal total = reliabilityScore.add(timelyRateScore)
                .add(rectificationRateScore).add(complaintRateScore)
                .add(restoreEfficiencyScore).add(reportCompletionScore)
                .setScale(2, RoundingMode.HALF_UP);
        assessment.setTotalScore(total);

        // 等级评定
        assessment.setAssessmentGrade(computeGrade(total));

        assessment.setAssessmentStatus(1); // 已计算
        assessment.setCalcTime(LocalDateTime.now());
    }

    /** 供电可靠率得分 (25分制) */
    private BigDecimal computeReliabilityScore() {
        // 模拟：99.5%-99.99%，可靠率越高得分越高
        double rs = 99.5 + RandomUtil.randomDouble(0, 0.49);
        double score = (rs - 99.5) / 0.49 * 25;
        return BigDecimal.valueOf(Math.min(score, 25))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** 故障处理及时率得分 (20分制) */
    private BigDecimal computeTimelyRateScore() {
        double rate = 85 + RandomUtil.randomDouble(0, 15);
        double score = rate / 100.0 * 20;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /** 隐患整治率得分 (20分制) */
    private BigDecimal computeRectificationRateScore() {
        double rate = 70 + RandomUtil.randomDouble(0, 30);
        double score = rate / 100.0 * 20;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /** 用户投诉率得分 (15分制，投诉越少得分越高) */
    private BigDecimal computeComplaintRateScore() {
        int complaints = RandomUtil.randomInt(0, 20);
        double score = Math.max(0, 15 - complaints * 0.75);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /** 停电复电效率得分 (10分制) */
    private BigDecimal computeRestoreEfficiencyScore() {
        double avgRestoreMin = RandomUtil.randomInt(30, 240);
        double score = Math.max(0, 10 - (avgRestoreMin / 240.0) * 10);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /** 报告完成率得分 (10分制) */
    private BigDecimal computeReportCompletionScore() {
        double rate = 80 + RandomUtil.randomDouble(0, 20);
        double score = rate / 100.0 * 10;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /** 等级评定 */
    private String computeGrade(BigDecimal total) {
        if (total.compareTo(BigDecimal.valueOf(90)) >= 0) return "A";
        if (total.compareTo(BigDecimal.valueOf(80)) >= 0) return "B";
        if (total.compareTo(BigDecimal.valueOf(60)) >= 0) return "C";
        return "D";
    }
}
