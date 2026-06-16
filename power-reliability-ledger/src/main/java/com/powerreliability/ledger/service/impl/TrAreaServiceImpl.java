package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.TrArea;
import com.powerreliability.ledger.mapper.TrAreaMapper;
import com.powerreliability.ledger.service.ITrAreaService;
import org.springframework.stereotype.Service;

@Service
public class TrAreaServiceImpl extends ServiceImpl<TrAreaMapper, TrArea> implements ITrAreaService {
}
