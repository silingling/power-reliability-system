package com.powerreliability.common.util;

import com.powerreliability.common.annotation.Excel;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用 Excel 导出工具（基于 Apache POI + @Excel 注解）
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {}

    /**
     * 导出 Excel 到 HttpServletResponse
     *
     * @param response  HTTP 响应
     * @param data      数据列表
     * @param fileName  导出文件名（不含扩展名）
     * @param <T>       实体类型
     */
    public static <T> void export(HttpServletResponse response, List<T> data, String fileName) {
        export(response, data, fileName, null);
    }

    /**
     * 导出 Excel 到 HttpServletResponse
     *
     * @param response  HTTP 响应
     * @param data      数据列表
     * @param fileName  导出文件名（不含扩展名）
     * @param sheetName 工作表名称
     * @param <T>       实体类型
     */
    public static <T> void export(HttpServletResponse response, List<T> data, String fileName, String sheetName) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("导出数据为空");
        }

        Class<?> clazz = data.get(0).getClass();
        List<Field> fields = getSortedExcelFields(clazz);
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("实体类 " + clazz.getSimpleName() + " 未配置 @Excel 注解字段");
        }

        // 使用 SXSSFWorkbook 支持大数据量导出
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.setCompressTempFiles(true);

        try {
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Sheet1");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                Excel excel = field.getAnnotation(Excel.class);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(excel.name());
                cell.setCellStyle(headerStyle);
                if (excel.width() > 0) {
                    sheet.setColumnWidth(i, excel.width() * 256);
                }
            }

            // 写入数据行
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (int rowIdx = 0; rowIdx < data.size(); rowIdx++) {
                Row row = sheet.createRow(rowIdx + 1);
                T item = data.get(rowIdx);

                for (int colIdx = 0; colIdx < fields.size(); colIdx++) {
                    Field field = fields.get(colIdx);
                    field.setAccessible(true);
                    Excel excel = field.getAnnotation(Excel.class);

                    Cell cell = row.createCell(colIdx);
                    cell.setCellStyle(dataStyle);

                    try {
                        Object value = field.get(item);
                        if (value == null) {
                            cell.setCellValue("");
                        } else if (value instanceof String s) {
                            cell.setCellValue(s);
                        } else if (value instanceof Number num) {
                            if (excel.useThousandsSeparator()) {
                                CellStyle numStyle = workbook.createCellStyle();
                                numStyle.cloneStyleFrom(dataStyle);
                                numStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
                                cell.setCellStyle(numStyle);
                            }
                            cell.setCellValue(num.doubleValue());
                        } else if (value instanceof LocalDateTime ldt) {
                            cell.setCellValue(ldt.format(dateTimeFormatter));
                        } else if (value instanceof LocalDate ld) {
                            cell.setCellValue(ld.format(dateFormatter));
                        } else if (value instanceof Date d) {
                            cell.setCellValue(d);
                        } else if (value instanceof Boolean b) {
                            cell.setCellValue(b ? "是" : "否");
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } catch (IllegalAccessException e) {
                        cell.setCellValue("");
                    }
                }
            }

            // 输出到 response
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败", e);
        } finally {
            // 清理临时文件
            workbook.dispose();
        }
    }

    /**
     * 获取类中所有被 @Excel 注解标注并按 sort 排序的字段
     */
    private static List<Field> getSortedExcelFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        // 包括父类字段
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Excel.class)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        // 按 sort 排序
        fields.sort(Comparator.comparingInt(f -> f.getAnnotation(Excel.class).sort()));
        return fields;
    }
}
