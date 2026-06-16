package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.ledger.entity.Line;
import com.powerreliability.ledger.service.ILineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger/line")
@Tag(name = "线路管理")
public class LineController {

    @Autowired
    private ILineService lineService;

    @GetMapping("/list")
    @Operation(summary = "分页查询线路列表")
    public Result<PageResult<Line>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String lineName,
            @RequestParam(required = false) String lineCode) {
        Page<Line> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Line> wrapper = new LambdaQueryWrapper<>();
        if (lineName != null && !lineName.isEmpty()) {
            wrapper.like(Line::getLineName, lineName);
        }
        if (lineCode != null && !lineCode.isEmpty()) {
            wrapper.like(Line::getLineCode, lineCode);
        }
        wrapper.orderByDesc(Line::getCreateTime);
        Page<Line> result = lineService.page(pageParam, wrapper);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize()));
    }

    @PostMapping("/add")
    @Operation(summary = "新增线路")
    public Result<Void> add(@RequestBody Line line) {
        lineService.save(line);
        return Result.ok();
    }

    @PutMapping("/update")
    @Operation(summary = "更新线路")
    public Result<Void> update(@RequestBody Line line) {
        lineService.updateById(line);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除线路")
    public Result<Void> delete(@PathVariable Long id) {
        lineService.removeById(id);
        return Result.ok();
    }
}
