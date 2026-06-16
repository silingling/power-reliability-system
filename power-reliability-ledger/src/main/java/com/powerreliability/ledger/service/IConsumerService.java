package com.powerreliability.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.ledger.entity.Consumer;

import java.util.List;

public interface IConsumerService extends IService<Consumer> {

    /**
     * 根据用户状态查询
     */
    List<Consumer> findByStatus(Integer status);

    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 根据台区编码查询用户
     */
    List<Consumer> findByAreaCode(String areaCode);

    /**
     * 查询高频停电用户（年停电次数超过阈值）
     */
    List<Consumer> findHighOutageUsers(Integer threshold);
}
