package com.powerreliability.outage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.outage.entity.OutageArchive;
import com.powerreliability.outage.mapper.OutageArchiveMapper;
import com.powerreliability.outage.service.OutageArchiveService;
import org.springframework.stereotype.Service;

@Service
public class OutageArchiveServiceImpl extends ServiceImpl<OutageArchiveMapper, OutageArchive> implements OutageArchiveService {
}
