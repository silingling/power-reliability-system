package com.powerreliability.index.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.index.entity.IndexAlert;

/**
 * 指标异常预警服务
 */
public interface IndexAlertService extends IService<IndexAlert> {

    /**
     * 检查阈值——对所有指标进行阈值检查，生成预警记录
     *
     * @return 新生成的预警数量
     */
    int checkThresholds();

    /**
     * 处理预警
     *
     * @param id      预警ID
     * @param measures 处理措施
     */
    void handleAlert(Long id, String measures);
}
