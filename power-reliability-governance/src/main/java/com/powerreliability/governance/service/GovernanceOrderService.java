package com.powerreliability.governance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.governance.entity.GovernanceOrder;

/**
 * 治理工单服务
 */
public interface GovernanceOrderService extends IService<GovernanceOrder> {

    /**
     * 分配工单
     *
     * @param id        工单ID
     * @param unit      负责单位
     * @param person    负责人
     * @param deadline  截止日期
     */
    void dispatch(Long id, String unit, String person, String deadline);

    /**
     * 提交审核
     *
     * @param id 工单ID
     */
    void submitForReview(Long id);

    /**
     * 审核工单
     *
     * @param id     工单ID
     * @param result 审核结果: 1-通过, 2-驳回
     */
    void acceptReview(Long id, Integer result);
}
