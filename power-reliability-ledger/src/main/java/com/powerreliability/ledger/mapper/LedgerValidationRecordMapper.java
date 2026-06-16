package com.powerreliability.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powerreliability.ledger.entity.LedgerValidationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 台账合规校验记录 Mapper
 */
@Mapper
public interface LedgerValidationRecordMapper extends BaseMapper<LedgerValidationRecord> {
}
