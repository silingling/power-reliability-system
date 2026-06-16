package com.powerreliability.index.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.index.service.ReliabilityIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可靠性指标统计控制器
 */
@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
@Tag(name = "可靠性指标管理")
public class ReliabilityIndexController {

    private final ReliabilityIndexService reliabilityIndexService;
    private final ReliabilityIndexMapper reliabilityIndexMapper;

    /**
     * 分页查询指标
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询可靠性指标")
    public Result<Page<ReliabilityIndex>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long statType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Integer period) {
        Page<ReliabilityIndex> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        if (statType != null) {
            wrapper.eq(ReliabilityIndex::getStatType, statType);
        }
        if (targetId != null) {
            wrapper.eq(ReliabilityIndex::getTargetId, targetId);
        }
        if (period != null) {
            wrapper.eq(ReliabilityIndex::getPeriod, period);
        }
        wrapper.orderByDesc(ReliabilityIndex::getStatDate);
        return Result.success(reliabilityIndexService.page(page, wrapper));
    }

    /**
     * 触发指标计算
     */
    @PostMapping("/calculate")
    @Operation(summary = "触发指标计算")
    public Result<Long> calculate(@RequestBody Map<String, Object> params) {
        Long statType = Long.parseLong(params.get("statType").toString());
        Long targetId = Long.parseLong(params.get("targetId").toString());
        Integer period = Integer.parseInt(params.get("period").toString());
        LocalDate start = LocalDate.parse(params.get("start").toString());
        LocalDate end = params.get("end") != null ? LocalDate.parse(params.get("end").toString()) : start;
        Long id = reliabilityIndexService.calculateIndex(statType, targetId, period, start, end);
        return Result.success(id);
    }

    /**
     * 趋势对比查询
     */
    @GetMapping("/compare")
    @Operation(summary = "趋势对比查询")
    public Result<List<ReliabilityIndex>> compare(
            @RequestParam Long statType,
            @RequestParam Long targetId,
            @RequestParam Integer period,
            @RequestParam String start,
            @RequestParam String end) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReliabilityIndex::getStatType, statType)
               .eq(ReliabilityIndex::getTargetId, targetId)
               .eq(ReliabilityIndex::getPeriod, period)
               .between(ReliabilityIndex::getStatDate, LocalDate.parse(start), LocalDate.parse(end))
               .orderByAsc(ReliabilityIndex::getStatDate);
        return Result.success(reliabilityIndexService.list(wrapper));
    }

    /**
     * 导出指标Excel
     */
    @PostMapping("/export")
    @Operation(summary = "导出可靠性指标Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Long statType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        if (statType != null) {
            wrapper.eq(ReliabilityIndex::getStatType, statType);
        }
        if (targetId != null) {
            wrapper.eq(ReliabilityIndex::getTargetId, targetId);
        }
        if (period != null) {
            wrapper.eq(ReliabilityIndex::getPeriod, period);
        }
        if (start != null && !start.isEmpty()) {
            wrapper.ge(ReliabilityIndex::getStatDate, LocalDate.parse(start));
        }
        if (end != null && !end.isEmpty()) {
            wrapper.le(ReliabilityIndex::getStatDate, LocalDate.parse(end));
        }
        wrapper.orderByDesc(ReliabilityIndex::getStatDate);
        List<ReliabilityIndex> list = reliabilityIndexService.list(wrapper);
        ExcelExportUtil.export(response, list, "可靠性指标导出");
    }

    // ==================== 标准化合规报表模板（4.4.4） ====================

    /**
     * 生成月度合规报表
     * 格式：每日明细 + 月度汇总 + 阈值标记
     */
    @PostMapping("/report/monthly")
    @Operation(summary = "导出月度可靠性合规报表")
    public void exportMonthlyReport(
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletResponse response) throws Exception {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<ReliabilityIndex> records = reliabilityIndexMapper.selectByDateRange(monthStart, monthEnd);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("月度可靠性报表");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subHeaderStyle = workbook.createCellStyle();
            Font subFont = workbook.createFont();
            subFont.setBold(true);
            subHeaderStyle.setFont(subFont);
            subHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle warnStyle = workbook.createCellStyle();
            warnStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            warnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dangerStyle = workbook.createCellStyle();
            dangerStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            dangerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 标题行
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("低压供电可靠性月度报表 - %d年%02d月", year, month));
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7));

            // 列标题
            String[] columns = {"日期", "SAIDI(分钟)", "SAIFI(次/户)", "CAIDI(分钟)", "ASAI(%)", "ENS(kWh)", "AENS(kWh/户)", "阈值标记"};
            Row colRow = sheet.createRow(1);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = colRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(subHeaderStyle);
            }

            // 数据行
            int rowNum = 2;
            double totalSaidi = 0, totalSaifi = 0, totalEns = 0;
            int count = 0, thresholdDays = 0;

            for (ReliabilityIndex idx : records) {
                Row row = sheet.createRow(rowNum);
                String dateStr = idx.getStatDate() != null
                        ? idx.getStatDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : "";
                double saidi = idx.getSaidi() != null ? idx.getSaidi() : 0;
                double saifi = idx.getSaifi() != null ? idx.getSaifi() : 0;
                double caidi = idx.getCaidi() != null ? idx.getCaidi() : 0;
                double asai = idx.getAsai() != null ? idx.getAsai() : 0;
                double ens = idx.getEns() != null ? idx.getEns() : 0;
                double aens = idx.getAens() != null ? idx.getAens() : 0;

                row.createCell(0).setCellValue(dateStr);
                row.createCell(1).setCellValue(saidi);
                row.createCell(2).setCellValue(saifi);
                row.createCell(3).setCellValue(caidi);
                row.createCell(4).setCellValue(asai);
                row.createCell(5).setCellValue(ens);
                row.createCell(6).setCellValue(aens);

                // 阈值标记
                boolean saidiFlag = saidi > 60;
                boolean saifiFlag = saifi > 0.5;
                String flag = "";
                if (saidiFlag && saifiFlag) {
                    flag = "SAIDI超标 & SAIFI超标";
                    for (int i = 0; i <= 6; i++) row.getCell(i).setCellStyle(dangerStyle);
                } else if (saidiFlag) {
                    flag = "SAIDI超标";
                    for (int i = 0; i <= 6; i++) row.getCell(i).setCellStyle(warnStyle);
                } else if (saifiFlag) {
                    flag = "SAIFI超标";
                    for (int i = 0; i <= 6; i++) row.getCell(i).setCellStyle(warnStyle);
                }
                row.createCell(7).setCellValue(flag);

                if (saidiFlag || saifiFlag) thresholdDays++;

                totalSaidi += saidi;
                totalSaifi += saifi;
                totalEns += ens;
                count++;
                rowNum++;
            }

            // 月度汇总行
            rowNum++;
            Row summaryRow = sheet.createRow(rowNum);
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            summaryRow.createCell(0).setCellValue("月度汇总");
            summaryRow.getCell(0).setCellStyle(summaryStyle);
            summaryRow.createCell(1).setCellValue(Math.round(totalSaidi * 100.0) / 100.0);
            summaryRow.getCell(1).setCellStyle(summaryStyle);
            summaryRow.createCell(2).setCellValue(Math.round(totalSaifi * 10000.0) / 10000.0);
            summaryRow.getCell(2).setCellStyle(summaryStyle);
            summaryRow.createCell(3).setCellValue(count > 0 ? Math.round((totalSaidi / count) * 100.0) / 100.0 : 0);
            summaryRow.getCell(3).setCellStyle(summaryStyle);
            summaryRow.createCell(4).setCellValue(count > 0 ? Math.round((totalSaidi / Math.max(count * 1440, 1)) * 10000) / 100.0 : 0);
            summaryRow.getCell(4).setCellStyle(summaryStyle);
            summaryRow.createCell(5).setCellValue(Math.round(totalEns * 100.0) / 100.0);
            summaryRow.getCell(5).setCellStyle(summaryStyle);
            summaryRow.createCell(6).setCellValue(0);
            summaryRow.getCell(6).setCellStyle(summaryStyle);
            summaryRow.createCell(7).setCellValue(thresholdDays + "天触发阈值");
            summaryRow.getCell(7).setCellStyle(summaryStyle);

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode(
                    String.format("月度可靠性报表_%d年%02d月.xlsx", year, month),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 生成季度合规报表
     * 格式：逐月明细 + 季度汇总 + 阈值天数统计
     */
    @PostMapping("/report/quarterly")
    @Operation(summary = "导出季度可靠性合规报表")
    public void exportQuarterlyReport(
            @RequestParam Integer year,
            @RequestParam Integer quarter,
            HttpServletResponse response) throws Exception {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = quarter * 3;
        LocalDate quarterStart = LocalDate.of(year, startMonth, 1);
        LocalDate quarterEnd = LocalDate.of(year, endMonth, YearMonth.of(year, endMonth).lengthOfMonth());

        List<ReliabilityIndex> records = reliabilityIndexMapper.selectByDateRange(quarterStart, quarterEnd);
        // 按月分组
        Map<Integer, List<ReliabilityIndex>> monthGroup = records.stream()
                .collect(Collectors.groupingBy(r -> r.getStatDate().getMonthValue()));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("季度可靠性报表");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subHeaderStyle = workbook.createCellStyle();
            Font subFont = workbook.createFont();
            subFont.setBold(true);
            subHeaderStyle.setFont(subFont);
            subHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("低压供电可靠性季度报表 - %d年第%d季度", year, quarter));
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            String[] columns = {"月份", "平均SAIDI(分钟)", "平均SAIFI(次/户)", "SAIDI合计", "SAIFI合计", "阈值超标天数"};
            Row colRow = sheet.createRow(1);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = colRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(subHeaderStyle);
            }

            double qtrTotalSaidi = 0, qtrTotalSaifi = 0;
            int qtrThresholdDays = 0, monthCount = 0, rowNum = 2;

            for (int m = startMonth; m <= endMonth; m++) {
                List<ReliabilityIndex> monthRecords = monthGroup.getOrDefault(m, Collections.emptyList());
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.format("%d年%d月", year, m));

                double avgSaidi = monthRecords.stream()
                        .filter(r -> r.getSaidi() != null)
                        .mapToDouble(ReliabilityIndex::getSaidi)
                        .average().orElse(0);
                double avgSaifi = monthRecords.stream()
                        .filter(r -> r.getSaifi() != null)
                        .mapToDouble(ReliabilityIndex::getSaifi)
                        .average().orElse(0);
                double sumSaidi = monthRecords.stream()
                        .filter(r -> r.getSaidi() != null)
                        .mapToDouble(ReliabilityIndex::getSaidi)
                        .sum();
                double sumSaifi = monthRecords.stream()
                        .filter(r -> r.getSaifi() != null)
                        .mapToDouble(ReliabilityIndex::getSaifi)
                        .sum();
                long thresholdDays = monthRecords.stream()
                        .filter(r -> (r.getSaidi() != null && r.getSaidi() > 60)
                                || (r.getSaifi() != null && r.getSaifi() > 0.5))
                        .count();

                row.createCell(1).setCellValue(Math.round(avgSaidi * 100.0) / 100.0);
                row.createCell(2).setCellValue(Math.round(avgSaifi * 10000.0) / 10000.0);
                row.createCell(3).setCellValue(Math.round(sumSaidi * 100.0) / 100.0);
                row.createCell(4).setCellValue(Math.round(sumSaifi * 10000.0) / 10000.0);
                row.createCell(5).setCellValue(thresholdDays);

                qtrTotalSaidi += sumSaidi;
                qtrTotalSaifi += sumSaifi;
                qtrThresholdDays += thresholdDays;
                monthCount++;
            }

            // 季度汇总
            rowNum++;
            Row qtrRow = sheet.createRow(rowNum);
            qtrRow.createCell(0).setCellValue("季度汇总");
            qtrRow.getCell(0).setCellStyle(summaryStyle);
            qtrRow.createCell(1).setCellValue(
                    monthCount > 0 ? Math.round((qtrTotalSaidi / monthCount) * 100.0) / 100.0 : 0);
            qtrRow.getCell(1).setCellStyle(summaryStyle);
            qtrRow.createCell(2).setCellValue(
                    monthCount > 0 ? Math.round((qtrTotalSaifi / monthCount) * 10000.0) / 10000.0 : 0);
            qtrRow.getCell(2).setCellStyle(summaryStyle);
            qtrRow.createCell(3).setCellValue(Math.round(qtrTotalSaidi * 100.0) / 100.0);
            qtrRow.getCell(3).setCellStyle(summaryStyle);
            qtrRow.createCell(4).setCellValue(Math.round(qtrTotalSaifi * 10000.0) / 10000.0);
            qtrRow.getCell(4).setCellStyle(summaryStyle);
            qtrRow.createCell(5).setCellValue(qtrThresholdDays);
            qtrRow.getCell(5).setCellStyle(summaryStyle);

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode(
                    String.format("季度可靠性报表_%d年Q%d.xlsx", year, quarter),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 生成年度合规报表
     * Sheet1: 月度明细 + 同比分析  Sheet2: 初步热点统计
     */
    @PostMapping("/report/annual")
    @Operation(summary = "导出年度可靠性合规报表")
    public void exportAnnualReport(
            @RequestParam Integer year,
            HttpServletResponse response) throws Exception {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        List<ReliabilityIndex> records = reliabilityIndexMapper.selectByDateRange(yearStart, yearEnd);

        // 上年度同比数据
        LocalDate prevYearStart = LocalDate.of(year - 1, 1, 1);
        LocalDate prevYearEnd = LocalDate.of(year - 1, 12, 31);
        List<ReliabilityIndex> prevRecords = reliabilityIndexMapper.selectByDateRange(prevYearStart, prevYearEnd);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            // ===== Sheet 1: 年度汇总 =====
            Sheet mainSheet = workbook.createSheet("年度汇总");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle subHeaderStyle = workbook.createCellStyle();
            Font subFont = workbook.createFont();
            subFont.setBold(true);
            subHeaderStyle.setFont(subFont);
            subHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = mainSheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(String.format("低压供电可靠性年度报表 - %d年", year));
            titleCell.setCellStyle(headerStyle);
            mainSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

            String[] mainCols = {"月份", "平均SAIDI(分钟)", "平均SAIFI(次/户)", "平均CAIDI(分钟)",
                    "去年SAIDI", "去年SAIFI", "SAIDI同比变化", "阈值超标天数"};
            Row colRow = mainSheet.createRow(1);
            for (int i = 0; i < mainCols.length; i++) {
                Cell cell = colRow.createCell(i);
                cell.setCellValue(mainCols[i]);
                cell.setCellStyle(subHeaderStyle);
            }

            Map<Integer, List<ReliabilityIndex>> monthGroup = records.stream()
                    .collect(Collectors.groupingBy(r -> r.getStatDate().getMonthValue()));
            Map<Integer, List<ReliabilityIndex>> prevMonthGroup = prevRecords.stream()
                    .collect(Collectors.groupingBy(r -> r.getStatDate().getMonthValue()));

            double yearTotalSaidi = 0, yearTotalSaifi = 0;
            int yearThresholdDays = 0, monthCount = 0, rowNum = 2;

            for (int m = 1; m <= 12; m++) {
                List<ReliabilityIndex> monthRecords = monthGroup.getOrDefault(m, Collections.emptyList());
                List<ReliabilityIndex> prevMonthRecords = prevMonthGroup.getOrDefault(m, Collections.emptyList());
                Row row = mainSheet.createRow(rowNum++);

                row.createCell(0).setCellValue(String.format("%d年%d月", year, m));

                double avgSaidi = monthRecords.stream()
                        .filter(r -> r.getSaidi() != null)
                        .mapToDouble(ReliabilityIndex::getSaidi)
                        .average().orElse(0);
                double avgSaifi = monthRecords.stream()
                        .filter(r -> r.getSaifi() != null)
                        .mapToDouble(ReliabilityIndex::getSaifi)
                        .average().orElse(0);
                double avgCaid = monthRecords.stream()
                        .filter(r -> r.getCaidi() != null)
                        .mapToDouble(ReliabilityIndex::getCaidi)
                        .average().orElse(0);
                long thresholdDays = monthRecords.stream()
                        .filter(r -> (r.getSaidi() != null && r.getSaidi() > 60)
                                || (r.getSaifi() != null && r.getSaifi() > 0.5))
                        .count();

                double prevAvgSaidi = prevMonthRecords.stream()
                        .filter(r -> r.getSaidi() != null)
                        .mapToDouble(ReliabilityIndex::getSaidi)
                        .average().orElse(0);
                double prevAvgSaifi = prevMonthRecords.stream()
                        .filter(r -> r.getSaifi() != null)
                        .mapToDouble(ReliabilityIndex::getSaifi)
                        .average().orElse(0);

                double saidiChange = prevAvgSaidi > 0 ? (avgSaidi - prevAvgSaidi) / prevAvgSaidi * 100 : 0;
                String changeStr = String.format("%.1f%%", Math.abs(saidiChange));
                changeStr = saidiChange > 0 ? "↑" + changeStr : (saidiChange < 0 ? "↓" + changeStr : "持平");

                row.createCell(1).setCellValue(Math.round(avgSaidi * 100.0) / 100.0);
                row.createCell(2).setCellValue(Math.round(avgSaifi * 10000.0) / 10000.0);
                row.createCell(3).setCellValue(Math.round(avgCaid * 100.0) / 100.0);
                row.createCell(4).setCellValue(Math.round(prevAvgSaidi * 100.0) / 100.0);
                row.createCell(5).setCellValue(Math.round(prevAvgSaifi * 10000.0) / 10000.0);
                row.createCell(6).setCellValue(changeStr);
                row.createCell(7).setCellValue(thresholdDays);

                yearTotalSaidi += avgSaidi;
                yearTotalSaifi += avgSaifi;
                yearThresholdDays += thresholdDays;
                monthCount++;
            }

            // 年度汇总行
            rowNum++;
            Row yearRow = mainSheet.createRow(rowNum);
            yearRow.createCell(0).setCellValue("年度汇总");
            yearRow.getCell(0).setCellStyle(summaryStyle);
            yearRow.createCell(1).setCellValue(
                    monthCount > 0 ? Math.round((yearTotalSaidi / monthCount) * 100.0) / 100.0 : 0);
            yearRow.getCell(1).setCellStyle(summaryStyle);
            yearRow.createCell(2).setCellValue(
                    monthCount > 0 ? Math.round((yearTotalSaifi / monthCount) * 10000.0) / 10000.0 : 0);
            yearRow.getCell(2).setCellStyle(summaryStyle);
            yearRow.createCell(3).setCellValue(0);
            yearRow.getCell(3).setCellStyle(summaryStyle);
            yearRow.createCell(4).setCellValue("—");
            yearRow.getCell(4).setCellStyle(summaryStyle);
            yearRow.createCell(5).setCellValue("—");
            yearRow.getCell(5).setCellStyle(summaryStyle);
            yearRow.createCell(6).setCellValue("—");
            yearRow.getCell(6).setCellStyle(summaryStyle);
            yearRow.createCell(7).setCellValue(yearThresholdDays);
            yearRow.getCell(7).setCellStyle(summaryStyle);

            for (int i = 0; i < mainCols.length; i++) mainSheet.autoSizeColumn(i);

            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode(
                    String.format("年度可靠性报表_%d年.xlsx", year),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + filename);
            workbook.write(response.getOutputStream());
        }
    }
}
