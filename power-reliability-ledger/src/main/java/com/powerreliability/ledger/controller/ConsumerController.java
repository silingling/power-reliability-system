package com.powerreliability.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.powerreliability.common.entity.PageResult;
import com.powerreliability.common.entity.Result;
import com.powerreliability.ledger.entity.Consumer;
import com.powerreliability.ledger.service.IConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/api/ledger")
public class ConsumerController {

    @Autowired
    private IConsumerService consumerService;

    /**
     * 分页查询用户
     */
    @GetMapping("/consumer/list")
    public Result<PageResult<Consumer>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String consumerNo,
            @RequestParam(required = false) String consumerName,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) Integer consumerType,
            @RequestParam(required = false) Integer status) {
        Page<Consumer> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(consumerNo)) {
            wrapper.like(Consumer::getConsumerNo, consumerNo);
        }
        if (StringUtils.hasText(consumerName)) {
            wrapper.like(Consumer::getConsumerName, consumerName);
        }
        if (StringUtils.hasText(areaCode)) {
            wrapper.eq(Consumer::getAreaCode, areaCode);
        }
        if (consumerType != null) {
            wrapper.eq(Consumer::getConsumerType, consumerType);
        }
        if (status != null) {
            wrapper.eq(Consumer::getStatus, status);
        }
        wrapper.orderByDesc(Consumer::getUpdateTime);
        consumerService.page(pageParam, wrapper);
        PageResult<Consumer> pageResult = PageResult.of(pageParam.getRecords(), pageParam.getTotal(), page, pageSize);
        return Result.ok(pageResult);
    }

    /**
     * 新增用户
     */
    @PostMapping("/consumer/add")
    public Result<Void> add(@RequestBody Consumer consumer) {
        consumerService.save(consumer);
        return Result.ok();
    }

    /**
     * 更新用户
     */
    @PutMapping("/consumer/update")
    public Result<Void> update(@RequestBody Consumer consumer) {
        consumerService.updateById(consumer);
        return Result.ok();
    }

    /**
     * 用户详情
     */
    @GetMapping("/consumer/detail/{id}")
    public Result<Consumer> detail(@PathVariable Long id) {
        Consumer consumer = consumerService.getById(id);
        return Result.ok(consumer);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/consumer/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        consumerService.removeById(id);
        return Result.ok();
    }

    /**
     * 绑定用户到台区
     */
    @PostMapping("/consumer/bind-area")
    public Result<Void> bindArea(@RequestBody BindAreaRequest request) {
        LambdaUpdateWrapper<Consumer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Consumer::getId, request.getConsumerId())
               .set(Consumer::getAreaId, request.getAreaId())
               .set(Consumer::getAreaCode, request.getAreaCode());
        consumerService.update(wrapper);
        return Result.ok();
    }

    /**
     * 绑定台区请求参数
     */
    public static class BindAreaRequest {
        private Long consumerId;
        private Long areaId;
        private String areaCode;

        public Long getConsumerId() { return consumerId; }
        public void setConsumerId(Long consumerId) { this.consumerId = consumerId; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    }
}
