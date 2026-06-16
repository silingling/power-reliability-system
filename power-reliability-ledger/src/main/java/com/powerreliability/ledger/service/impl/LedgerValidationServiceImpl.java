package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.*;
import com.powerreliability.ledger.mapper.*;
import com.powerreliability.ledger.service.ILedgerValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 台账合规智能校验引擎 实现
 */
@Service
public class LedgerValidationServiceImpl
        extends ServiceImpl<LedgerValidationRecordMapper, LedgerValidationRecord>
        implements ILedgerValidationService {

    @Autowired
    private TrAreaMapper trAreaMapper;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private LineMapper lineMapper;

    // ------------------- 公共入口 -------------------

    @Override
    public List<LedgerValidationRecord> validateAll() {
        List<LedgerValidationRecord> all = new ArrayList<>();
        all.addAll(validateCompleteness());
        all.addAll(validateLogic());
        all.addAll(validateStandard());
        return all;
    }

    @Override
    public List<LedgerValidationRecord> validateTable(String tableName) {
        List<LedgerValidationRecord> records = new ArrayList<>();
        switch (tableName.toLowerCase()) {
            case "tr_area":
                records.addAll(validateTrAreaCompleteness());
                records.addAll(validateTrAreaLogic());
                records.addAll(validateTrAreaStandard());
                break;
            case "consumer":
                records.addAll(validateConsumerCompleteness());
                records.addAll(validateConsumerLogic());
                records.addAll(validateConsumerStandard());
                break;
            case "equipment":
                records.addAll(validateEquipmentCompleteness());
                records.addAll(validateEquipmentLogic());
                records.addAll(validateEquipmentStandard());
                break;
            case "line":
                records.addAll(validateLineCompleteness());
                records.addAll(validateLineLogic());
                records.addAll(validateLineStandard());
                break;
            default:
                throw new IllegalArgumentException("不支持的台账表名: " + tableName);
        }
        return records;
    }

    // =================== 完整性校验 ===================

    @Override
    public List<LedgerValidationRecord> validateCompleteness() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        records.addAll(validateTrAreaCompleteness());
        records.addAll(validateConsumerCompleteness());
        records.addAll(validateEquipmentCompleteness());
        records.addAll(validateLineCompleteness());
        return records;
    }

    // -- 台区完整性 --

    private List<LedgerValidationRecord> validateTrAreaCompleteness() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<TrArea> list = trAreaMapper.selectList(null);
        String table = "tr_area";
        for (TrArea entity : list) {
            checkBlank(table, entity.getId(), "areaCode", entity.getAreaCode(), records);
            checkBlank(table, entity.getId(), "areaName", entity.getAreaName(), records);
            checkBlank(table, entity.getId(), "substationName", entity.getSubstationName(), records);
            checkBlank(table, entity.getId(), "responsiblePerson", entity.getResponsiblePerson(), records);
            checkNull(table, entity.getId(), "commissioningDate", entity.getCommissioningDate(), records);
            checkNull(table, entity.getId(), "designCapacity", entity.getDesignCapacity(), records);
            checkBlank(table, entity.getId(), "transformerModel", entity.getTransformerModel(), records);
        }
        return records;
    }

    // -- 用户完整性 --

    private List<LedgerValidationRecord> validateConsumerCompleteness() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Consumer> list = consumerMapper.selectList(null);
        String table = "consumer";
        for (Consumer entity : list) {
            checkBlank(table, entity.getId(), "consumerNo", entity.getConsumerNo(), records);
            checkBlank(table, entity.getId(), "consumerName", entity.getConsumerName(), records);
            checkNull(table, entity.getId(), "areaId", entity.getAreaId(), records);
            checkNull(table, entity.getId(), "consumerType", entity.getConsumerType(), records);
            checkNull(table, entity.getId(), "openDate", entity.getOpenDate(), records);
        }
        return records;
    }

    // -- 设备完整性 --

    private List<LedgerValidationRecord> validateEquipmentCompleteness() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Equipment> list = equipmentMapper.selectList(null);
        String table = "equipment";
        for (Equipment entity : list) {
            checkBlank(table, entity.getId(), "equipmentCode", entity.getEquipmentCode(), records);
            checkBlank(table, entity.getId(), "equipmentName", entity.getEquipmentName(), records);
            checkNull(table, entity.getId(), "equipmentType", entity.getEquipmentType(), records);
            checkNull(table, entity.getId(), "commissioningDate", entity.getCommissioningDate(), records);
            checkBlank(table, entity.getId(), "locationDesc", entity.getLocationDesc(), records);
        }
        return records;
    }

    // -- 线路完整性 --

    private List<LedgerValidationRecord> validateLineCompleteness() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Line> list = lineMapper.selectList(null);
        String table = "line";
        for (Line entity : list) {
            checkBlank(table, entity.getId(), "lineCode", entity.getLineCode(), records);
            checkBlank(table, entity.getId(), "lineName", entity.getLineName(), records);
            checkNull(table, entity.getId(), "areaId", entity.getAreaId(), records);
            checkBlank(table, entity.getId(), "voltageLevel", entity.getVoltageLevel(), records);
        }
        return records;
    }

    // =================== 逻辑性校验 ===================

    @Override
    public List<LedgerValidationRecord> validateLogic() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        records.addAll(validateTrAreaLogic());
        records.addAll(validateConsumerLogic());
        records.addAll(validateEquipmentLogic());
        records.addAll(validateLineLogic());
        return records;
    }

    // -- 台区逻辑 --

    private List<LedgerValidationRecord> validateTrAreaLogic() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<TrArea> list = trAreaMapper.selectList(null);
        LocalDate minDate = LocalDate.of(1990, 1, 1);
        String table = "tr_area";
        for (TrArea entity : list) {
            // commissioningDate must not be before 1990-01-01
            if (entity.getCommissioningDate() != null && entity.getCommissioningDate().isBefore(minDate)) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "commissioningDate",
                        entity.getCommissioningDate().toString(), "投运日期不能早于1990-01-01", "ERROR"));
            }
            // currentLoad must not exceed ratedLoad
            if (entity.getCurrentLoad() != null && entity.getRatedLoad() != null
                    && entity.getCurrentLoad().compareTo(entity.getRatedLoad()) > 0) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "currentLoad",
                        "当前负荷=" + entity.getCurrentLoad() + ", 额定负荷=" + entity.getRatedLoad(),
                        "当前在运负荷不能超过额定负荷", "ERROR"));
            }
            // ratedLoad must not exceed designCapacity
            if (entity.getRatedLoad() != null && entity.getDesignCapacity() != null
                    && entity.getRatedLoad().compareTo(entity.getDesignCapacity()) > 0) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "ratedLoad",
                        "额定负荷=" + entity.getRatedLoad() + ", 设计容量=" + entity.getDesignCapacity(),
                        "额定负荷不能超过设计容量", "WARNING"));
            }
        }
        return records;
    }

    // -- 用户逻辑 --

    private List<LedgerValidationRecord> validateConsumerLogic() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Consumer> list = consumerMapper.selectList(null);
        LocalDate minDate = LocalDate.of(1950, 1, 1);
        String table = "consumer";
        for (Consumer entity : list) {
            // openDate must not be before 1950-01-01
            if (entity.getOpenDate() != null && entity.getOpenDate().isBefore(minDate)) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "openDate",
                        entity.getOpenDate().toString(), "开户时间不能早于1950-01-01", "ERROR"));
            }
            // closeDate if set must be after openDate
            if (entity.getCloseDate() != null && entity.getOpenDate() != null
                    && entity.getCloseDate().isBefore(entity.getOpenDate())) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "closeDate",
                        "closeDate=" + entity.getCloseDate() + ", openDate=" + entity.getOpenDate(),
                        "销户时间不能早于开户时间", "ERROR"));
            }
            // status and closeDate consistency: if status indicates closed (0) but closeDate is null
            if (entity.getStatus() != null && entity.getStatus() == 0 && entity.getCloseDate() == null) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "status",
                        "status=0(销户)", "用户状态为销户但无销户时间", "ERROR"));
            }
            // if closeDate is set but status is not 0
            if (entity.getCloseDate() != null && (entity.getStatus() == null || entity.getStatus() != 0)) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "status",
                        "closeDate=" + entity.getCloseDate() + ", status=" + entity.getStatus(),
                        "用户有销户时间但状态不为销户", "WARNING"));
            }
        }
        return records;
    }

    // -- 设备逻辑 --

    private List<LedgerValidationRecord> validateEquipmentLogic() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Equipment> list = equipmentMapper.selectList(null);
        String table = "equipment";
        for (Equipment entity : list) {
            // manufactureDate must be before commissioningDate
            if (entity.getManufactureDate() != null && entity.getCommissioningDate() != null
                    && entity.getManufactureDate().isAfter(entity.getCommissioningDate())) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "manufactureDate",
                        "出厂日期=" + entity.getManufactureDate() + ", 投运日期=" + entity.getCommissioningDate(),
                        "出厂日期不能晚于投运日期", "ERROR"));
            }
            // lastInspectionDate must not be before commissioningDate
            if (entity.getLastInspectionDate() != null && entity.getCommissioningDate() != null
                    && entity.getLastInspectionDate().isBefore(entity.getCommissioningDate())) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "lastInspectionDate",
                        "最近年检日期=" + entity.getLastInspectionDate() + ", 投运日期=" + entity.getCommissioningDate(),
                        "最近年检日期不能早于投运日期", "ERROR"));
            }
            // faultCount >= 0
            if (entity.getFaultCount() != null && entity.getFaultCount() < 0) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "faultCount",
                        String.valueOf(entity.getFaultCount()), "故障次数不能为负数", "ERROR"));
            }
        }
        return records;
    }

    // -- 线路逻辑 --

    private List<LedgerValidationRecord> validateLineLogic() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Line> list = lineMapper.selectList(null);
        String table = "line";
        for (Line entity : list) {
            // totalLength > 0
            if (entity.getTotalLength() == null || entity.getTotalLength().compareTo(BigDecimal.ZERO) <= 0) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "totalLength",
                        entity.getTotalLength() == null ? "null" : entity.getTotalLength().toString(),
                        "线路总长度必须大于0", "ERROR"));
            }
            // voltageLevel should be "0.4kV" for low voltage
            if (StringUtils.hasText(entity.getVoltageLevel())
                    && !"0.4kV".equals(entity.getVoltageLevel().trim())) {
                records.add(buildRecord(table, entity.getId(), "LOGICAL", "voltageLevel",
                        entity.getVoltageLevel(), "低压线路电压等级应为0.4kV", "WARNING"));
            }
        }
        return records;
    }

    // =================== 规范性校验 ===================

    @Override
    public List<LedgerValidationRecord> validateStandard() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        records.addAll(validateTrAreaStandard());
        records.addAll(validateConsumerStandard());
        records.addAll(validateEquipmentStandard());
        records.addAll(validateLineStandard());
        return records;
    }

    private static final Pattern AREA_CODE_PATTERN = Pattern.compile("TA\\d{6}");
    private static final Pattern CONSUMER_NO_PATTERN = Pattern.compile("C\\d{10}");
    private static final Pattern EQUIPMENT_CODE_PATTERN = Pattern.compile("EQ\\d{8}");
    private static final Pattern LINE_CODE_PATTERN = Pattern.compile("LN\\d{6}");

    private List<LedgerValidationRecord> validateTrAreaStandard() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<TrArea> list = trAreaMapper.selectList(null);
        String table = "tr_area";
        for (TrArea entity : list) {
            if (!StringUtils.hasText(entity.getAreaCode())) continue;
            if (!AREA_CODE_PATTERN.matcher(entity.getAreaCode()).matches()) {
                records.add(buildRecord(table, entity.getId(), "STANDARD", "areaCode",
                        entity.getAreaCode(), "台区编码格式不正确，应为 TA + 6位数字（如 TA000001）", "ERROR"));
            }
        }
        return records;
    }

    private List<LedgerValidationRecord> validateConsumerStandard() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Consumer> list = consumerMapper.selectList(null);
        String table = "consumer";
        for (Consumer entity : list) {
            if (!StringUtils.hasText(entity.getConsumerNo())) continue;
            if (!CONSUMER_NO_PATTERN.matcher(entity.getConsumerNo()).matches()) {
                records.add(buildRecord(table, entity.getId(), "STANDARD", "consumerNo",
                        entity.getConsumerNo(), "用户户号格式不正确，应为 C + 10位数字（如 C0000000001）", "ERROR"));
            }
        }
        return records;
    }

    private List<LedgerValidationRecord> validateEquipmentStandard() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Equipment> list = equipmentMapper.selectList(null);
        String table = "equipment";
        for (Equipment entity : list) {
            if (!StringUtils.hasText(entity.getEquipmentCode())) continue;
            if (!EQUIPMENT_CODE_PATTERN.matcher(entity.getEquipmentCode()).matches()) {
                records.add(buildRecord(table, entity.getId(), "STANDARD", "equipmentCode",
                        entity.getEquipmentCode(), "设备编码格式不正确，应为 EQ + 8位数字（如 EQ00000001）", "ERROR"));
            }
        }
        return records;
    }

    private List<LedgerValidationRecord> validateLineStandard() {
        List<LedgerValidationRecord> records = new ArrayList<>();
        List<Line> list = lineMapper.selectList(null);
        String table = "line";
        for (Line entity : list) {
            if (!StringUtils.hasText(entity.getLineCode())) continue;
            if (!LINE_CODE_PATTERN.matcher(entity.getLineCode()).matches()) {
                records.add(buildRecord(table, entity.getId(), "STANDARD", "lineCode",
                        entity.getLineCode(), "线路编码格式不正确，应为 LN + 6位数字（如 LN000001）", "ERROR"));
            }
        }
        return records;
    }

    // =================== 工具方法 ===================

    private void checkBlank(String table, Long targetId, String fieldName, String value,
                            List<LedgerValidationRecord> records) {
        if (!StringUtils.hasText(value)) {
            records.add(buildRecord(table, targetId, "COMPLETENESS", fieldName,
                    value, fieldName + " 为空", "ERROR"));
        }
    }

    private void checkNull(String table, Long targetId, String fieldName, Object value,
                           List<LedgerValidationRecord> records) {
        if (value == null) {
            records.add(buildRecord(table, targetId, "COMPLETENESS", fieldName,
                    "null", fieldName + " 为空", "ERROR"));
        }
    }

    private LedgerValidationRecord buildRecord(String targetTable, Long targetId,
                                                String validationType, String fieldName,
                                                String fieldValue, String issueDesc,
                                                String severityLevel) {
        LedgerValidationRecord record = new LedgerValidationRecord();
        record.setTargetTable(targetTable);
        record.setTargetId(targetId);
        record.setValidationType(validationType);
        record.setFieldName(fieldName);
        record.setFieldValue(fieldValue);
        record.setIssueDesc(issueDesc);
        record.setSeverityLevel(severityLevel);
        record.setStatus(0);
        return record;
    }
}
