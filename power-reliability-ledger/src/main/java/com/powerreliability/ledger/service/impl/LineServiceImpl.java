package com.powerreliability.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.ledger.entity.Line;
import com.powerreliability.ledger.mapper.LineMapper;
import com.powerreliability.ledger.service.ILineService;
import org.springframework.stereotype.Service;

@Service
public class LineServiceImpl extends ServiceImpl<LineMapper, Line> implements ILineService {
}
