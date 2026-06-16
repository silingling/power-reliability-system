package com.powerreliability.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.review.entity.RectificationTask;

import java.util.List;

/**
 * 整改任务服务
 */
public interface RectificationTaskService extends IService<RectificationTask> {

    /**
     * 根据复盘报告中暴露的问题自动生成整改任务
     * @param reportId 复盘报告ID
     * @return 生成的整改任务列表
     */
    List<RectificationTask> createFromReport(Long reportId);

    /**
     * 完成整改任务
     * @param id   任务ID
     * @param desc 完成描述
     * @return true/false
     */
    boolean complete(Long id, String desc);

    /**
     * 验收整改任务
     * @param id     任务ID
     * @param result 验收结果：1-通过 2-不通过
     * @param opinion 验收意见
     * @return true/false
     */
    boolean accept(Long id, Integer result, String opinion);
}
