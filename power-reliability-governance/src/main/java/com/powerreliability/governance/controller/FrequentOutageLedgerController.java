package com.powerreliability.governance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.Result;
import com.powerreliability.governance.entity.FrequentOutageLedger;
import com.powerreliability.governance.service.FrequentOutageLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @PostMapping("/ledger/list")
    public Result<Page<FrequentOutageLedger>> list(@RequestBody Map<String, Object> params) {
        Integer pageNum = params.get("pageNum") != null ? Integer.parseInt(params.get("pageNum").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

        Page<FrequentOutageLedger> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FrequentOutageLedger> wrapper = new LambdaQueryWrapper<>();

        if (params.get("riskLevel") != null && !params.get("riskLevel").toString().isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getRiskLevel, params.get("riskLevel").toString());
        }
        if (params.get("governanceStatus") != null && !params.get("governanceStatus").toString().isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getGovernanceStatus, params.get("governanceStatus").toString());
        }
        if (params.get("areaCode") != null && !params.get("areaCode").toString().isEmpty()) {
            wrapper.eq(FrequentOutageLedger::getAreaCode, params.get("areaCode").toString());
        }

        wrapper.orderByDesc(FrequentOutageLedger::getCreateTime);
        return Result.success(frequentOutageLedgerService.page(page, wrapper));
    }

    /**
     * 自动筛查
     */
    @PostMapping("/ledger/auto-screen")
    public Result<Integer> autoScreen() {
        int count = frequentOutageLedgerService.autoScreen();
        return Result.success(count);
    }

    /**
     * 查询详情
     */
    @PostMapping("/ledger/detail/{id}")
    public Result<FrequentOutageLedger> detail(@PathVariable Long id) {
        FrequentOutageLedger record = frequentOutageLedgerService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success(record);
    }
}
