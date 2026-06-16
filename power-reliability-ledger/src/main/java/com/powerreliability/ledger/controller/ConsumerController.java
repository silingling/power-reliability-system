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
    @PostMapping("/consumer/list")
    public Result<PageResult<Consumer>> list(@RequestBody ConsumerQuery query) {
        Page<Consumer> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Consumer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getConsumerNo())) {
            wrapper.like(Consumer::getConsumerNo, query.getConsumerNo());
        }
        if (StringUtils.hasText(query.getConsumerName())) {
            wrapper.like(Consumer::getConsumerName, query.getConsumerName());
        }
        if (StringUtils.hasText(query.getAreaCode())) {
            wrapper.eq(Consumer::getAreaCode, query.getAreaCode());
        }
        if (query.getConsumerType() != null) {
            wrapper.eq(Consumer::getConsumerType, query.getConsumerType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Consumer::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Consumer::getUpdateTime);
        consumerService.page(page, wrapper);
        PageResult<Consumer> pageResult = PageResult.of(page.getRecords(), page.getTotal(), query.getPage(), query.getPageSize());
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
    @PostMapping("/consumer/update")
    public Result<Void> update(@RequestBody Consumer consumer) {
        consumerService.updateById(consumer);
        return Result.ok();
    }

    /**
     * 用户详情
     */
    @PostMapping("/consumer/detail/{id}")
    public Result<Consumer> detail(@PathVariable Long id) {
        Consumer consumer = consumerService.getById(id);
        return Result.ok(consumer);
    }

    /**
     * 删除用户
     */
    @PostMapping("/consumer/delete/{id}")
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
     * 用户分页查询参数
     */
    public static class ConsumerQuery {
        private int page = 1;
        private int pageSize = 20;
        private String consumerNo;
        private String consumerName;
        private String areaCode;
        private Integer consumerType;
        private Integer status;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public String getConsumerNo() { return consumerNo; }
        public void setConsumerNo(String consumerNo) { this.consumerNo = consumerNo; }
        public String getConsumerName() { return consumerName; }
        public void setConsumerName(String consumerName) { this.consumerName = consumerName; }
        public String getAreaCode() { return areaCode; }
        public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
        public Integer getConsumerType() { return consumerType; }
        public void setConsumerType(Integer consumerType) { this.consumerType = consumerType; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
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
