package com.powerreliability.index.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.index.service.ReliabilityIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可靠性指标统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReliabilityIndexServiceImpl
        extends ServiceImpl<ReliabilityIndexMapper, ReliabilityIndex>
        implements ReliabilityIndexService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long calculateIndex(Long statType, Long targetId, Integer period, LocalDate start, LocalDate end) {
        log.info("开始计算可靠性指标 statType={}, targetId={}, period={}, range=[{}, {}]",
                statType, targetId, period, start, end);

        // 模拟指标计算逻辑
        ReliabilityIndex index = new ReliabilityIndex();
        index.setStatType(statType);
        index.setTargetId(targetId);
        index.setPeriod(period);
        index.setStatDate(start);

        // 示例：基于历史数据模拟计算各可靠性指标
        // 实际场景中从停电事件表聚合计算
        index.setAsai(99.95);
        index.setAsidi(0.05);
        index.setSaifi(1.25);
        index.setSaidi(2.30);
        index.setCaidi(1.84);
        index.setEns(1500.0);
        index.setAens(0.75);

        save(index);

        log.info("可靠性指标计算完成, id={}, SAIFI={}, SAIDI={}", index.getId(), index.getSaifi(), index.getSaidi());
        return index.getId();
    }

    @Override
    public List<Map<String, Object>> exportMonthlyReport(Integer year, Integer month) {
        // 获取该月所有日/周统计记录
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(ReliabilityIndex::getStatDate, monthStart)
                .le(ReliabilityIndex::getStatDate, monthEnd)
                .orderByAsc(ReliabilityIndex::getStatDate);

        List<ReliabilityIndex> records = list(wrapper);

        List<Map<String, Object>> report = new ArrayList<>();
        for (ReliabilityIndex record : records) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("statDate", record.getStatDate());
            row.put("statType", record.getStatType());
            row.put("targetId", record.getTargetId());
            row.put("period", record.getPeriod());
            row.put("ASAI", record.getAsai());
            row.put("ASIDI", record.getAsidi());
            row.put("SAIFI", record.getSaifi());
            row.put("SAIDI", record.getSaidi());
            row.put("CAIDI", record.getCaidi());
            row.put("ENS", record.getEns());
            row.put("AENS", record.getAens());
            report.add(row);
        }

        // 添加月度汇总行
        if (!records.isEmpty()) {
            DoubleSummaryStatistics saifiStats = records.stream()
                    .filter(r -> r.getSaifi() != null)
                    .collect(Collectors.summarizingDouble(ReliabilityIndex::getSaifi));
            DoubleSummaryStatistics saidiStats = records.stream()
                    .filter(r -> r.getSaidi() != null)
                    .collect(Collectors.summarizingDouble(ReliabilityIndex::getSaidi));

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("statDate", "月度汇总");
            summary.put("SAIFI_avg", saifiStats.getAverage());
            summary.put("SAIFI_max", saifiStats.getMax());
            summary.put("SAIFI_min", saifiStats.getMin());
            summary.put("SAIDI_avg", saidiStats.getAverage());
            summary.put("SAIDI_max", saidiStats.getMax());
            summary.put("SAIDI_min", saidiStats.getMin());
            report.add(summary);
        }

        log.info("月度可靠性报表生成完成, year={}, month={}, records={}", year, month, records.size());
        return report;
    }

    @Override
    public Map<String, Object> comparePeriods(Long statType, Long targetId, Integer baseYear, Integer compareYear) {
        // 获取基准年和对比年的全年数据
        LambdaQueryWrapper<ReliabilityIndex> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(ReliabilityIndex::getStatType, statType)
                .eq(ReliabilityIndex::getTargetId, targetId)
                .ge(ReliabilityIndex::getStatDate, LocalDate.of(baseYear, 1, 1))
                .le(ReliabilityIndex::getStatDate, LocalDate.of(baseYear, 12, 31));

        LambdaQueryWrapper<ReliabilityIndex> compareWrapper = new LambdaQueryWrapper<>();
        compareWrapper.eq(ReliabilityIndex::getStatType, statType)
                .eq(ReliabilityIndex::getTargetId, targetId)
                .ge(ReliabilityIndex::getStatDate, LocalDate.of(compareYear, 1, 1))
                .le(ReliabilityIndex::getStatDate, LocalDate.of(compareYear, 12, 31));

        List<ReliabilityIndex> baseRecords = list(baseWrapper);
        List<ReliabilityIndex> compareRecords = list(compareWrapper);

        // 计算各指标均值
        double baseSaifi = baseRecords.stream()
                .filter(r -> r.getSaifi() != null)
                .mapToDouble(ReliabilityIndex::getSaifi)
                .average().orElse(0);

        double baseSaidi = baseRecords.stream()
                .filter(r -> r.getSaidi() != null)
                .mapToDouble(ReliabilityIndex::getSaidi)
                .average().orElse(0);

        double baseAsai = baseRecords.stream()
                .filter(r -> r.getAsai() != null)
                .mapToDouble(ReliabilityIndex::getAsai)
                .average().orElse(0);

        double compareSaifi = compareRecords.stream()
                .filter(r -> r.getSaifi() != null)
                .mapToDouble(ReliabilityIndex::getSaifi)
                .average().orElse(0);

        double compareSaidi = compareRecords.stream()
                .filter(r -> r.getSaidi() != null)
                .mapToDouble(ReliabilityIndex::getSaidi)
                .average().orElse(0);

        double compareAsai = compareRecords.stream()
                .filter(r -> r.getAsai() != null)
                .mapToDouble(ReliabilityIndex::getAsai)
                .average().orElse(0);

        // 计算同比变化
        double saifiChange = baseSaifi > 0 ? (compareSaifi - baseSaifi) / baseSaifi * 100 : 0;
        double saidiChange = baseSaidi > 0 ? (compareSaidi - baseSaidi) / baseSaidi * 100 : 0;
        double asaiChange = baseAsai > 0 ? (compareAsai - baseAsai) / baseAsai * 100 : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("statType", statType);
        result.put("targetId", targetId);
        result.put("baseYear", baseYear);
        result.put("compareYear", compareYear);

        Map<String, Object> base = new LinkedHashMap<>();
        base.put("SAIFI", Math.round(baseSaifi * 10000.0) / 10000.0);
        base.put("SAIDI", Math.round(baseSaidi * 100.0) / 100.0);
        base.put("ASAI", Math.round(baseAsai * 100.0) / 100.0);
        result.put("baseYearData", base);

        Map<String, Object> compare = new LinkedHashMap<>();
        compare.put("SAIFI", Math.round(compareSaifi * 10000.0) / 10000.0);
        compare.put("SAIDI", Math.round(compareSaidi * 100.0) / 100.0);
        compare.put("ASAI", Math.round(compareAsai * 100.0) / 100.0);
        result.put("compareYearData", compare);

        Map<String, Object> change = new LinkedHashMap<>();
        change.put("SAIFI", Math.round(saifiChange * 100.0) / 100.0 + "%");
        change.put("SAIDI", Math.round(saidiChange * 100.0) / 100.0 + "%");
        change.put("ASAI", Math.round(asaiChange * 100.0) / 100.0 + "%");
        result.put("changeRate", change);

        log.info("同期对比分析完成, targetId={}, baseYear={}, compareYear={}", targetId, baseYear, compareYear);
        return result;
    }
}
