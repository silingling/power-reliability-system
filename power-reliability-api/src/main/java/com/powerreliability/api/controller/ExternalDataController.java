package com.powerreliability.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.powerreliability.common.entity.Result;
import com.powerreliability.governance.entity.FrequentOutageLedger;
import com.powerreliability.governance.mapper.FrequentOutageLedgerMapper;
import com.powerreliability.index.entity.ReliabilityIndex;
import com.powerreliability.index.mapper.ReliabilityIndexMapper;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.entity.Equipment;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.mapper.ConsumerMapper;
import com.powerreliability.ledger.mapper.EquipmentMapper;
import com.powerreliability.ledger.mapper.TrAreaMapper;
import com.powerreliability.outage.entity.OutageEvent;
import com.powerreliability.outage.mapper.OutageEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 外部系统数据对接接口（5.5）
 * 营销系统 / PMS2.0 / 调度系统 / 抢修平台 / 气象系统
 */
@Slf4j
@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
@Tag(name = "外部系统对接接口")
public class ExternalDataController {

    private final ConsumerMapper consumerMapper;
    private final EquipmentMapper equipmentMapper;
    private final TrAreaMapper trAreaMapper;
    private final OutageEventMapper outageEventMapper;
    private final FrequentOutageLedgerMapper frequentOutageLedgerMapper;
    private final ReliabilityIndexMapper reliabilityIndexMapper;

    // ========== 1. 营销系统对接 ==========

    @PostMapping("/sync/user")
    @Operation(summary = "同步营销系统用户档案")
    public Result<Map<String, Object>> syncConsumerData(@RequestBody List<ExternalConsumer> consumers) {
        log.info("[外部接口] 接收营销系统用户数据 {} 条", consumers.size());
        int success = 0, failed = 0;
        for (ExternalConsumer ec : consumers) {
            try {
                Consumer entity = new Consumer();
                entity.setConsumerNo(ec.getConsumerNo());
                entity.setConsumerName(ec.getConsumerName());
                entity.setConsumerAddress(ec.getConsumerAddress());
                entity.setConsumerType(ec.getConsumerType());
                entity.setAreaCode(ec.getAreaCode());
                entity.setLineCode(ec.getLineCode());
                entity.setContactPhone(ec.getContactPhone());
                if (ec.getOpenDate() != null) {
                    entity.setOpenDate(LocalDate.parse(ec.getOpenDate(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
                if (ec.getCloseDate() != null) {
                    entity.setCloseDate(LocalDate.parse(ec.getCloseDate(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
                entity.setStatus(1);
                consumerMapper.insert(entity);
                success++;
            } catch (Exception e) {
                log.error("[外部接口] 同步用户失败: consumerNo={}", ec.getConsumerNo(), e);
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", consumers.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("syncTime", LocalDateTime.now().toString());
        return Result.ok(result);
    }

    @GetMapping("/sync/user/status")
    @Operation(summary = "查询用户数据同步状态")
    public Result<Map<String, Object>> getSyncStatus(@RequestParam(required = false) String date) {
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        if (date != null && !date.isEmpty()) {
            wrapper.ge(Consumer::getCreateTime, LocalDate.parse(date).atStartOfDay());
        }
        long total = consumerMapper.selectCount(wrapper);
        Map<String, Object> status = new HashMap<>();
        status.put("lastSyncTime", LocalDateTime.now().toString());
        status.put("totalUsers", total);
        status.put("pendingSync", 0);
        return Result.ok(status);
    }

    // ========== 2. PMS2.0配网系统对接 ==========

    @PostMapping("/sync/equipment")
    @Operation(summary = "同步PMS设备台账")
    public Result<Map<String, Object>> syncEquipment(@RequestBody List<ExternalEquipment> equipments) {
        log.info("[外部接口] 接收PMS设备数据 {} 条", equipments.size());
        int success = 0, failed = 0;
        for (ExternalEquipment ee : equipments) {
            try {
                Equipment entity = new Equipment();
                entity.setEquipmentCode(ee.getEquipmentCode());
                entity.setEquipmentName(ee.getEquipmentName());
                entity.setEquipmentType(ee.getEquipmentType());
                entity.setEquipmentModel(ee.getEquipmentModel());
                entity.setManufacturer(ee.getManufacturer());
                if (ee.getManufactureDate() != null) {
                    entity.setManufactureDate(LocalDate.parse(ee.getManufactureDate(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
                if (ee.getCommissioningDate() != null) {
                    entity.setCommissioningDate(LocalDate.parse(ee.getCommissioningDate(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
                entity.setAreaId(ee.getAreaId());
                entity.setEquipmentStatus(1);
                equipmentMapper.insert(entity);
                success++;
            } catch (Exception e) {
                log.error("[外部接口] 同步设备失败: code={}", ee.getEquipmentCode(), e);
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", equipments.size());
        result.put("success", success);
        result.put("failed", failed);
        return Result.ok(result);
    }

    @PostMapping("/sync/area")
    @Operation(summary = "同步PMS台区档案")
    public Result<Map<String, Object>> syncArea(@RequestBody List<ExternalArea> areas) {
        log.info("[外部接口] 接收PMS台区数据 {} 条", areas.size());
        int success = 0, failed = 0;
        for (ExternalArea ea : areas) {
            try {
                TrArea entity = new TrArea();
                entity.setAreaCode(ea.getAreaCode());
                entity.setAreaName(ea.getAreaName());
                entity.setSubstationName(ea.getSubstationName());
                entity.setResponsiblePerson(ea.getResponsiblePerson());
                if (ea.getCommissioningDate() != null) {
                    entity.setCommissioningDate(LocalDate.parse(ea.getCommissioningDate(), DateTimeFormatter.ISO_LOCAL_DATE));
                }
                if (ea.getDesignCapacity() != null) entity.setDesignCapacity(java.math.BigDecimal.valueOf(ea.getDesignCapacity()));
                if (ea.getRatedLoad() != null) entity.setRatedLoad(java.math.BigDecimal.valueOf(ea.getRatedLoad()));
                entity.setAreaStatus(1);
                trAreaMapper.insert(entity);
                success++;
            } catch (Exception e) {
                log.error("[外部接口] 同步台区失败: code={}", ea.getAreaCode(), e);
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", areas.size());
        result.put("success", success);
        result.put("failed", failed);
        return Result.ok(result);
    }

    // ========== 3. 调度系统对接 ==========

    @PostMapping("/sync/outage-event")
    @Operation(summary = "同步调度系统停电事件")
    public Result<Map<String, Object>> syncOutageEvent(@RequestBody List<ExternalOutageEvent> events) {
        log.info("[外部接口] 接收调度系统停电事件 {} 条", events.size());
        int success = 0, failed = 0;
        for (ExternalOutageEvent eoe : events) {
            try {
                OutageEvent entity = new OutageEvent();
                entity.setEventNo(eoe.getEventNo());
                entity.setAreaCode(eoe.getAreaCode());
                entity.setAreaName(eoe.getAreaName());
                entity.setOutageType(eoe.getOutageType());
                if (eoe.getOutageStartTime() != null) {
                    entity.setOutageStartTime(LocalDateTime.parse(eoe.getOutageStartTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                if (eoe.getOutageEndTime() != null) {
                    entity.setOutageEndTime(LocalDateTime.parse(eoe.getOutageEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                entity.setAffectedConsumerCount(eoe.getAffectedConsumers());
                entity.setFaultReason(eoe.getFaultReason());
                entity.setIsClosed(eoe.getIsClosed() != null ? eoe.getIsClosed() : 0);
                entity.setIsExempt(0);
                outageEventMapper.insert(entity);
                success++;
            } catch (Exception e) {
                log.error("[外部接口] 同步停电事件失败: eventNo={}", eoe.getEventNo(), e);
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", events.size());
        result.put("success", success);
        result.put("failed", failed);
        return Result.ok(result);
    }

    // ========== 4. 抢修平台对接 ==========

    @PostMapping("/sync/repair-order")
    @Operation(summary = "同步抢修平台工单")
    public Result<Map<String, Object>> syncRepairOrder(@RequestBody List<ExternalRepairOrder> orders) {
        log.info("[外部接口] 接收抢修平台工单 {} 条", orders.size());
        // 抢修工单更新到 OutageEvent 的 faultReason / repairMeasures
        int updated = 0, failed = 0;
        for (ExternalRepairOrder ero : orders) {
            try {
                LambdaQueryWrapper<OutageEvent> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OutageEvent::getEventNo, ero.getEventNo());
                OutageEvent event = outageEventMapper.selectOne(wrapper);
                if (event != null) {
                    event.setFaultReason(ero.getFaultFinding());
                    if (ero.getRepairCompleteTime() != null) {
                        event.setOutageEndTime(LocalDateTime.parse(ero.getRepairCompleteTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                    event.setIsClosed(1);
                    event.setUpdateTime(LocalDateTime.now());
                    outageEventMapper.updateById(event);
                    updated++;
                }
            } catch (Exception e) {
                log.error("[外部接口] 更新抢修数据失败: orderNo={}", ero.getOrderNo(), e);
                failed++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", orders.size());
        result.put("updated", updated);
        result.put("failed", failed);
        return Result.ok(result);
    }

    // ========== 5. 气象系统对接 ==========

    @PostMapping("/sync/weather")
    @Operation(summary = "同步气象预警数据")
    public Result<Map<String, Object>> syncWeather(@RequestBody List<ExternalWeatherData> weatherList) {
        log.info("[外部接口] 接收气象数据 {} 条", weatherList.size());
        // 气象数据记录到台区表的 defectInfo 字段（简化处理）
        int updated = 0;
        for (ExternalWeatherData ewd : weatherList) {
            try {
                if (ewd.getWarningLevel() != null && ewd.getWarningLevel() >= 3) {
                    LambdaQueryWrapper<TrArea> wrapper = new LambdaQueryWrapper<>();
                    wrapper.like(TrArea::getAreaCode, ewd.getStationCode());
                    List<TrArea> areas = trAreaMapper.selectList(wrapper);
                    for (TrArea area : areas) {
                        area.setDefectInfo("[气象预警] " + ewd.getWeatherDate() + " "
                                + ewd.getWeatherType() + " 预警级别" + ewd.getWarningLevel());
                        area.setUpdateTime(LocalDateTime.now());
                        trAreaMapper.updateById(area);
                        updated++;
                    }
                }
            } catch (Exception e) {
                log.error("[外部接口] 同步气象数据失败: station={}", ewd.getStationCode(), e);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("received", weatherList.size());
        result.put("matchedAreasUpdated", updated);
        return Result.ok(result);
    }

    // ========== 6. 数据导出接口 ==========

    @GetMapping("/export/governance-ledger")
    @Operation(summary = "对外导出频繁停电治理台账")
    public Result<List<Map<String, Object>>> exportGovernanceLedger(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<FrequentOutageLedger> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(FrequentOutageLedger::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(FrequentOutageLedger::getCreateTime, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(FrequentOutageLedger::getCreateTime);
        List<FrequentOutageLedger> ledgers = frequentOutageLedgerMapper.selectList(wrapper);

        List<Map<String, Object>> result = ledgers.stream().map(l -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("eventId", l.getEventId());
            row.put("areaCode", l.getAreaCode());
            row.put("areaName", l.getAreaName());
            row.put("outageStartTime", l.getOutageStartTime());
            row.put("outageDuration", l.getOutageDuration());
            row.put("affectedUsers", l.getAffectedUsers());
            row.put("riskLevel", l.getRiskLevel());
            row.put("governanceStatus", l.getGovernanceStatus());
            row.put("causeCategory", l.getCauseCategory());
            return row;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    @GetMapping("/export/reliability-index")
    @Operation(summary = "对外导出可靠性指标数据")
    public Result<List<Map<String, Object>>> exportReliabilityIndex(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<ReliabilityIndex> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(ReliabilityIndex::getStatDate, LocalDate.parse(startDate));
        }
        if (endDate != null) {
            wrapper.le(ReliabilityIndex::getStatDate, LocalDate.parse(endDate));
        }
        wrapper.orderByAsc(ReliabilityIndex::getStatDate);
        List<ReliabilityIndex> indices = reliabilityIndexMapper.selectList(wrapper);

        List<Map<String, Object>> result = indices.stream().map(idx -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("statDate", idx.getStatDate());
            row.put("statType", idx.getStatType());
            row.put("targetId", idx.getTargetId());
            row.put("period", idx.getPeriod());
            row.put("SAIFI", idx.getSaifi());
            row.put("SAIDI", idx.getSaidi());
            row.put("CAIDI", idx.getCaidi());
            row.put("ASAI", idx.getAsai());
            row.put("ENS", idx.getEns());
            row.put("AENS", idx.getAens());
            return row;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    // ========== 数据模型 ==========

    @Data
    static class ExternalConsumer {
        private String consumerNo;
        private String consumerName;
        private String consumerAddress;
        private Integer consumerType;
        private String areaCode;
        private String lineCode;
        private String contactPhone;
        private String openDate;
        private String closeDate;
    }

    @Data
    static class ExternalEquipment {
        private String equipmentCode;
        private String equipmentName;
        private Integer equipmentType;
        private String equipmentModel;
        private String manufacturer;
        private String manufactureDate;
        private String commissioningDate;
        private Long areaId;
    }

    @Data
    static class ExternalArea {
        private String areaCode;
        private String areaName;
        private String substationName;
        private String responsiblePerson;
        private String commissioningDate;
        private Double designCapacity;
        private Double ratedLoad;
    }

    @Data
    static class ExternalOutageEvent {
        private String eventNo;
        private String areaCode;
        private String areaName;
        private Integer outageType;
        private String outageStartTime;
        private String outageEndTime;
        private Integer affectedConsumers;
        private String faultReason;
        private Integer isClosed;
    }

    @Data
    static class ExternalRepairOrder {
        private String orderNo;
        private String eventNo;
        private String dispatchTime;
        private String arriveTime;
        private String repairTeam;
        private String repairCompleteTime;
        private String faultFinding;
        private String repairMeasures;
    }

    @Data
    static class ExternalWeatherData {
        private String stationCode;
        private String weatherDate;
        private String weatherType;
        private Double temperature;
        private Double windSpeed;
        private Double rainfall;
        private Integer warningLevel;
    }
}
