package com.powerreliability.review.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.review.entity.ReviewReport;
import com.powerreliability.review.mapper.ReviewReportMapper;
import com.powerreliability.review.service.ReviewReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ReviewReportServiceImpl extends ServiceImpl<ReviewReportMapper, ReviewReport>
        implements ReviewReportService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewReport createFromOutage(Long outageEventId) {
        log.info("根据停电事件[{}]生成复盘报告", outageEventId);

        // ========== 实际项目中，此处应远程调用 power-reliability-outage
        // 微服务获取 OutageEvent 详情，此处以模拟数据代替 ==========
        MockOutageData outage = mockOutageData(outageEventId);

        ReviewReport report = new ReviewReport();
        report.setReportNo(generateReportNo());
        report.setReportTitle(String.format("停电事件复盘报告 - %s - %s",
                DateUtil.format(outage.time, "yyyy-MM-dd"), outage.eventNo));
        report.setOutageEventId(outage.eventId);
        report.setOutageEventNo(outage.eventNo);
        report.setOutageTime(outage.time);
        report.setRestoreTime(outage.restoreTime);
        report.setOutageDuration(outage.duration);
        report.setOutageType(outage.outageType);
        report.setAffectedConsumerCount(outage.affectedCount);
        report.setOutageReason(outage.reason);
        report.setOutageProcess(buildProcess(outage));
        report.setCauseAnalysis(buildCauseAnalysis(outage));
        report.setIdentifiedProblems(buildIdentifiedProblems(outage));
        report.setRectificationSuggestion(buildSuggestion(outage));
        report.setIsExempt(outage.isExempt);
        report.setExemptType(outage.exemptType);
        report.setResponsibleUnit(outage.responsibleUnit);
        report.setReportStatus(0); // 草稿
        report.setReportPerson("系统自动生成");
        report.setRemarks("由系统根据停电事件数据自动生成");

        save(report);
        log.info("复盘报告生成完成，报告编号: {}", report.getReportNo());
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        ReviewReport report = getById(id);
        if (report == null) {
            log.warn("报告不存在: {}", id);
            return false;
        }
        if (report.getReportStatus() != 0) {
            log.warn("报告状态不是草稿，当前状态: {}", report.getReportStatus());
            return false;
        }
        report.setReportStatus(1); // 已发布
        report.setPublishTime(LocalDateTime.now());
        return updateById(report);
    }

    // ==================== 辅助方法 ====================

    private String generateReportNo() {
        return "RP" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
                + RandomUtil.randomNumbers(4);
    }

    private String buildProcess(MockOutageData outage) {
        return String.format(
                "%s，%s区域发生%s，影响%d户用户。\n" +
                "停电时间：%s\n" +
                "复电时间：%s\n" +
                "停电时长：%d分钟\n" +
                "经过排查，原因为：%s",
                DateUtil.format(outage.time, "yyyy年MM月dd日HH时mm分"),
                outage.areaName,
                outage.outageType == 1 ? "计划停电" : outage.outageType == 2 ? "故障停电" : "临时停电",
                outage.affectedCount,
                DateUtil.format(outage.time, "yyyy-MM-dd HH:mm:ss"),
                DateUtil.format(outage.restoreTime, "yyyy-MM-dd HH:mm:ss"),
                outage.duration,
                outage.reason);
    }

    private String buildCauseAnalysis(MockOutageData outage) {
        if (outage.isExempt == 1) {
            return "本次停电经认定为豁免事件，不纳入供电可靠性统计。豁免类型：" +
                    (outage.exemptType == 1 ? "上级电网原因" : "用户自身原因");
        }
        return String.format(
                "1. 直接原因：%s\n" +
                "2. 设备因素：设备运行年限较长，缺乏预防性维护\n" +
                "3. 管理因素：巡检不到位，未能及时发现隐患\n" +
                "4. 环境因素：雷雨天气等外部环境影响\n" +
                "5. 深层次原因：供电基础设施老化，技改投入不足",
                outage.reason);
    }

    private String buildIdentifiedProblems(MockOutageData outage) {
        return "本次停电暴露的主要问题：\n" +
                "1. 设备老旧：部分设备超期服役，故障率上升\n" +
                "2. 巡视不到位：日常巡检未能发现潜在隐患\n" +
                "3. 应急处置响应速度有待提升\n" +
                "4. 用户侧安全隐患较多，缺乏有效监管\n" +
                "5. 停电信息告知机制有待完善";
    }

    private String buildSuggestion(MockOutageData outage) {
        return "整改建议：\n" +
                "1. 对老旧设备进行排查，制定更新改造计划\n" +
                "2. 加强设备巡检频率，利用在线监测手段\n" +
                "3. 优化应急抢修流程，提升复电效率\n" +
                "4. 加强用户侧安全用电宣传和检查\n" +
                "5. 建立停电信息多渠道发布机制";
    }

    // ==================== 模拟数据 ====================

    private MockOutageData mockOutageData(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return new MockOutageData(
                id,
                "EVT" + DateUtil.format(now, "yyyyMMdd") + RandomUtil.randomNumbers(6),
                now.minusDays(RandomUtil.randomInt(1, 30)),
                now.minusDays(RandomUtil.randomInt(1, 30)).plusHours(RandomUtil.randomInt(1, 8)),
                RandomUtil.randomInt(30, 480),
                RandomUtil.randomInt(1, 4),
                RandomUtil.randomInt(50, 500),
                RandomUtil.randomEle(List.of(
                        "电缆老化短路", "变压器过载烧毁", "雷击损坏设备",
                        "外力施工挖断电缆", "用户内部故障越级跳闸")),
                "城区供电所",
                RandomUtil.randomInt(0, 2),
                RandomUtil.randomInt(0, 3));
    }

    private record MockOutageData(
            Long eventId, String eventNo, LocalDateTime time,
            LocalDateTime restoreTime, Integer duration, Integer outageType,
            Integer affectedCount, String reason, String areaName,
            Integer isExempt, Integer exemptType) {
    }

    private static <T> java.util.List<T> List(T... items) {
        return java.util.List.of(items);
    }
}
