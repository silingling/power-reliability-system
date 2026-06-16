package com.powerreliability.warning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.warning.entity.RiskPrediction;

/**
 * 隐患预判服务
 */
public interface RiskPredictionService extends IService<RiskPrediction> {

    /**
     * 自动执行多因素风险评分预判
     * 综合考虑设备老化、历史故障、负荷率、天气影响等因素
     * @return 本次生成的预判记录数
     */
    int autoPredict();
}
