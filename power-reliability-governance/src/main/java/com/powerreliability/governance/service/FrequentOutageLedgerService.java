package com.powerreliability.governance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.governance.entity.FrequentOutageLedger;

/**
 * 频繁停电台账服务
 */
public interface FrequentOutageLedgerService extends IService<FrequentOutageLedger> {

    /**
     * 自动筛查——基于配置阈值自动检测违规记录
     *
     * @return 筛查出的违规记录数
     */
    int autoScreen();
}
