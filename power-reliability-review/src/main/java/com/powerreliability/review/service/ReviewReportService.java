package com.powerreliability.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.review.entity.ReviewReport;

/**
 * 停电复盘报告服务
 */
public interface ReviewReportService extends IService<ReviewReport> {

    /**
     * 根据停电事件数据自动生成复盘报告
     * @param outageEventId 停电事件ID
     * @return 生成的报告
     */
    ReviewReport createFromOutage(Long outageEventId);

    /**
     * 发布复盘报告
     * @param id 报告ID
     * @return true/false
     */
    boolean publish(Long id);
}
