package com.powerreliability.warning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.warning.entity.RiskWarningOrder;

/**
 * 隐患预警工单服务
 */
public interface RiskWarningOrderService extends IService<RiskWarningOrder> {

    /**
     * 根据预判记录自动生成预警工单并下发
     * @return 生成的工单数
     */
    int dispatchWarning();

    /**
     * 处置预警工单
     * @param id  工单ID
     * @param desc 处置描述
     * @return true/false
     */
    boolean dispose(Long id, String desc);

    /**
     * 复核预警工单
     * @param id     工单ID
     * @param result 复核结果：1-通过 2-不通过
     * @return true/false
     */
    boolean review(Long id, Integer result);
}
