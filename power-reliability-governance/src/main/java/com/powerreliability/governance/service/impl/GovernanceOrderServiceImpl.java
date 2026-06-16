package com.powerreliability.governance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.powerreliability.governance.entity.GovernanceOrder;
import com.powerreliability.governance.exception.GovernanceException;
import com.powerreliability.governance.mapper.GovernanceOrderMapper;
import com.powerreliability.governance.service.GovernanceOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 治理工单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceOrderServiceImpl
        extends ServiceImpl<GovernanceOrderMapper, GovernanceOrder>
        implements GovernanceOrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispatch(Long id, String unit, String person, String deadline) {
        GovernanceOrder order = getById(id);
        if (order == null) {
            throw new GovernanceException("工单不存在: " + id);
        }
        if (order.getStatus() != 0) {
            throw new GovernanceException("工单状态不允许分配, 当前状态: " + order.getStatus());
        }

        order.setResponsibleUnit(unit);
        order.setResponsiblePerson(person);
        order.setDeadline(LocalDate.parse(deadline));
        order.setStatus(1);
        order.setDispatchTime(LocalDateTime.now());
        updateById(order);

        log.info("工单 {} 已分配至 {}/{}", order.getOrderNo(), unit, person);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long id) {
        GovernanceOrder order = getById(id);
        if (order == null) {
            throw new GovernanceException("工单不存在: " + id);
        }
        if (order.getStatus() != 1) {
            throw new GovernanceException("工单状态不允许提交审核, 当前状态: " + order.getStatus());
        }

        order.setStatus(2);
        order.setSubmitTime(LocalDateTime.now());
        updateById(order);

        log.info("工单 {} 已提交审核", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptReview(Long id, Integer result) {
        GovernanceOrder order = getById(id);
        if (order == null) {
            throw new GovernanceException("工单不存在: " + id);
        }
        if (order.getStatus() != 2) {
            throw new GovernanceException("工单状态不允许审核, 当前状态: " + order.getStatus());
        }
        if (result == null || (result != 1 && result != 2)) {
            throw new GovernanceException("审核结果无效: " + result + ", 仅允许 1(通过) 或 2(驳回)");
        }

        order.setReviewResult(result);
        order.setStatus(result == 1 ? 3 : 4);
        order.setReviewTime(LocalDateTime.now());
        updateById(order);

        log.info("工单 {} 审核完成, 结果: {}", order.getOrderNo(), result == 1 ? "通过" : "驳回");
    }
}
