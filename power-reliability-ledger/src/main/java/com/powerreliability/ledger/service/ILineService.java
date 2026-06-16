package com.powerreliability.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.powerreliability.ledger.entity.Line;

import java.util.List;

public interface ILineService extends IService<Line> {

    /**
     * 根据线路状态查询
     */
    List<Line> findByStatus(Integer status);

    /**
     * 批量更新线路状态
     */
    int batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 根据台区编码查询线路
     */
    List<Line> findByAreaCode(String areaCode);

    /**
     * 根据线路类型查询
     */
    List<Line> findByLineType(Integer lineType);
}
