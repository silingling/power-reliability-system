package com.powerreliability.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.ledger.entity.LedgerValidationRecord;

import java.util.List;

/**
 * 台账合规智能校验引擎 服务接口
 */
public interface ILedgerValidationService extends IService<LedgerValidationRecord> {

    /**
     * 对全部台账表执行所有维度校验（完整性 / 逻辑性 / 规范性）
     *
     * @return 本次校验产生的问题记录列表
     */
    List<LedgerValidationRecord> validateAll();

    /**
     * 对指定台账表执行所有维度校验
     *
     * @param tableName 表名（tr_area / consumer / equipment / line）
     * @return 本次校验产生的问题记录列表
     */
    List<LedgerValidationRecord> validateTable(String tableName);

    /**
     * 完整性校验：检查关键字段是否存在空 / 空字符串
     *
     * @return 完整性校验结果列表
     */
    List<LedgerValidationRecord> validateCompleteness();

    /**
     * 逻辑性校验：检查字段间逻辑矛盾
     *
     * @return 逻辑性校验结果列表
     */
    List<LedgerValidationRecord> validateLogic();

    /**
     * 规范性校验：检查字段编码格式是否符合规则
     *
     * @return 规范性校验结果列表
     */
    List<LedgerValidationRecord> validateStandard();
}
