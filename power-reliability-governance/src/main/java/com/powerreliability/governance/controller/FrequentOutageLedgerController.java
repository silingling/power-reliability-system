package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.governance.entity.FrequentOutageLedger;
import com.powerreliability.governance.service.FrequentOutageLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 频繁停电台账控制器
 */
@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
public class FrequentOutageLedgerController {

    private final FrequentOutageLedgerService frequentOutageLedgerService;

    /**
     * 分页查询台账
     */
    @GetMapping("/ledger/list")
    @Operation(summary = "分页查询频繁停电台账")
    public Result<Page<FrequentOutageLedger>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String governanceStatus,
            @RequestParam(required = false) String areaCode) {
        Page<FrequentOutageLedger> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FrequentOutageLedger> wrapper = new LambdaQueryWrapper<>();
        if (riskLevel != null && !riskLevel.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getRiskLevel, riskLevel);
        }
        if (governanceStatus != null && !governanceStatus.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getGovernanceStatus, governanceStatus);
        }
        if (areaCode != null && !areaCode.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getAreaCode, areaCode);
        }
        wrapper.orderByDesc(FrequentOutageLedger::getCreateTime);
        return Result.success(frequentOutageLedgerService.page(page, wrapper));
    }

    /**
     * 导出Excel
     */
    @PostMapping("/ledger/export")
    @Operation(summary = "导出频繁停电台账Excel")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String governanceStatus,
            @RequestParam(required = false) String areaCode) {
        LambdaQueryWrapper<FrequentOutageLedger> wrapper = new LambdaQueryWrapper<>();
        if (riskLevel != null && !riskLevel.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getRiskLevel, riskLevel);
        }
        if (governanceStatus != null && !governanceStatus.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getGovernanceStatus, governanceStatus);
        }
        if (areaCode != null && !areaCode.isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getAreaCode, areaCode);
        }
        wrapper.orderByDesc(FrequentOutageLedger::getCreateTime);
        List<FrequentOutageLedger> list = frequentOutageLedgerService.list(wrapper);
        ExcelExportUtil.export(response, list, "频繁停电台账导出");
    }

    /**
     * 自动筛查
     */
    @PostMapping("/ledger/auto-screen")
    @Operation(summary = "自动筛查频繁停电")
    public Result<Integer> autoScreen() {
        int count = frequentOutageLedgerService.autoScreen();
        return Result.success(count);
    }

    /**
     * 查询详情
     */
    @GetMapping("/ledger/detail/{id}")
    @Operation(summary = "查询台账详情")
    public Result<FrequentOutageLedger> detail(@PathVariable Long id) {
        FrequentOutageLedger record = frequentOutageLedgerService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success(record);
    }
}
