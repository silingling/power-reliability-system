package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.common.util.ExcelExportUtil;
import com.powerreliability.ledger.entity.LedgerValidationRecord;
import com.powerreliability.ledger.service.ILedgerValidationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 台账合规智能校验 控制器
 */
@RestController
@RequestMapping("/api/ledger/validate")
public class LedgerValidationController {

    @Autowired
    private ILedgerValidationService ledgerValidationService;

    /**
     * 执行全部台账表的全维度校验
     * POST /api/ledger/validate
     */
    @PostMapping
    public Result<List<LedgerValidationRecord>> validateAll() {
        List<LedgerValidationRecord> records = ledgerValidationService.validateAll();
        return Result.ok(records);
    }

    /**
     * 对指定台账表执行全维度校验
     * POST /api/ledger/validate/{table}
     *
     * @param table 表名（tr_area / consumer / equipment / line）
     */
    @PostMapping("/{table}")
    public Result<List<LedgerValidationRecord>> validateTable(@PathVariable String table) {
        List<LedgerValidationRecord> records = ledgerValidationService.validateTable(table);
        return Result.ok(records);
    }

    /**
     * 分页查询校验记录
     * GET /api/ledger/validate/records?page=1&pageSize=20&targetTable=tr_area&validationType=COMPLETENESS&status=0
     */
    @GetMapping("/records")
    public Result<PageResult<LedgerValidationRecord>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) String validationType,
            @RequestParam(required = false) Integer status) {
        Page<LedgerValidationRecord> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<LedgerValidationRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(targetTable)) {
            wrapper.eq(LedgerValidationRecord::getTargetTable, targetTable);
        }
        if (StringUtils.hasText(validationType)) {
            wrapper.eq(LedgerValidationRecord::getValidationType, validationType);
        }
        if (status != null) {
            wrapper.eq(LedgerValidationRecord::getStatus, status);
        }
        wrapper.orderByDesc(LedgerValidationRecord::getCreateTime);
        ledgerValidationService.page(pageParam, wrapper);
        PageResult<LedgerValidationRecord> pageResult =
                PageResult.of(pageParam.getRecords(), pageParam.getTotal(), page, pageSize);
        return Result.ok(pageResult);
    }

    /**
     * 导出校验记录到 Excel
     * GET /api/ledger/validate/records/export?targetTable=tr_area&validationType=COMPLETENESS&status=0
     */
    @GetMapping("/records/export")
    public void exportRecords(
            HttpServletResponse response,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) String validationType,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<LedgerValidationRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(targetTable)) {
            wrapper.eq(LedgerValidationRecord::getTargetTable, targetTable);
        }
        if (StringUtils.hasText(validationType)) {
            wrapper.eq(LedgerValidationRecord::getValidationType, validationType);
        }
        if (status != null) {
            wrapper.eq(LedgerValidationRecord::getStatus, status);
        }
        wrapper.orderByDesc(LedgerValidationRecord::getCreateTime);
        List<LedgerValidationRecord> records = ledgerValidationService.list(wrapper);
        ExcelExportUtil.export(response, records, "台账合规校验记录");
    }
}
