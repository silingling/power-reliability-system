package com.powerreliability.review.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.review.entity.RectificationTask;
import com.powerreliability.review.entity.ReviewReport;
import com.powerreliability.review.mapper.RectificationTaskMapper;
import com.powerreliability.review.service.RectificationTaskService;
import com.powerreliability.review.service.ReviewReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RectificationTaskServiceImpl extends ServiceImpl<RectificationTaskMapper, RectificationTask>
        implements RectificationTaskService {

    private final ReviewReportService reviewReportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RectificationTask> createFromReport(Long reportId) {
        log.info("根据复盘报告[{}]生成整改任务", reportId);

        ReviewReport report = reviewReportService.getById(reportId);
        if (report == null) {
            log.warn("复盘报告不存在: {}", reportId);
            return List.of();
        }
        if (report.getReportStatus() != 1) {
            log.warn("复盘报告未发布，不能生成整改任务，当前状态: {}", report.getReportStatus());
            return List.of();
        }

        // 检查是否已生成过
        long existingCount = lambdaQuery()
                .eq(RectificationTask::getReportId, reportId)
                .count();
        if (existingCount > 0) {
            log.warn("复盘报告[{}]已生成过整改任务，跳过", reportId);
            return List.of();
        }

        List<RectificationTask> tasks = new ArrayList<>();

        // 根据复盘报告中的暴露问题，生成 3-5 条整改任务
        tasks.add(buildTask(report, "设备更替改造",
                "对停电区域的老旧设备进行全面排查，制定设备更新改造计划并组织实施",
                1, "运检部", LocalDate.now().plusDays(30)));
        tasks.add(buildTask(report, "巡检制度加强",
                "优化巡检周期和巡检内容，增加红外测温、局放检测等带电检测手段",
                3, "运检部", LocalDate.now().plusDays(15)));
        tasks.add(buildTask(report, "应急抢修预案优化",
                "完善应急抢修预案，缩短到达现场时间和故障修复时间",
                4, "调度中心", LocalDate.now().plusDays(20)));
        tasks.add(buildTask(report, "用户侧安全检查",
                "对受影响用户开展用电安全专项检查，消除用户侧安全隐患",
                3, "营销部", LocalDate.now().plusDays(45)));

        saveBatch(tasks);
        log.info("整改任务生成完成，共 {} 条", tasks.size());
        return tasks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(Long id, String desc) {
        RectificationTask task = getById(id);
        if (task == null) {
            log.warn("整改任务不存在: {}", id);
            return false;
        }
        if (task.getTaskStatus() != 0 && task.getTaskStatus() != 1) {
            log.warn("整改任务状态不能完成，当前状态: {}", task.getTaskStatus());
            return false;
        }
        task.setTaskStatus(2); // 已完成
        task.setCompleteDesc(desc);
        task.setCompleteTime(LocalDateTime.now());
        return updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean accept(Long id, Integer result, String opinion) {
        RectificationTask task = getById(id);
        if (task == null) {
            log.warn("整改任务不存在: {}", id);
            return false;
        }
        if (task.getTaskStatus() != 2) {
            log.warn("整改任务尚未完成，当前状态: {}", task.getTaskStatus());
            return false;
        }
        task.setAcceptResult(result);
        task.setAcceptOpinion(opinion);
        task.setAcceptPerson("当前验收人");
        task.setAcceptTime(LocalDateTime.now());
        if (result == 1) {
            task.setTaskStatus(3); // 已验收
        } else {
            task.setTaskStatus(1); // 退回整改中
            task.setDeadline(task.getDeadline().plusDays(15)); // 延期15天
        }
        return updateById(task);
    }

    // ==================== 辅助方法 ====================

    private RectificationTask buildTask(ReviewReport report, String problemDesc,
                                         String measure, Integer type,
                                         String dept, LocalDate deadline) {
        RectificationTask task = new RectificationTask();
        task.setTaskNo("RECT" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
                + RandomUtil.randomNumbers(4));
        task.setReportId(report.getId());
        task.setOutageEventId(report.getOutageEventId());
        task.setProblemDesc(problemDesc);
        task.setRectificationMeasure(measure);
        task.setRectificationType(type);
        task.setResponsibleDept(dept);
        task.setResponsiblePerson("待分配");
        task.setDeadline(deadline);
        task.setTaskStatus(0); // 待整改
        return task;
    }
}
