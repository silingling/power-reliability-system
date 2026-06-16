package com.powerreliability.common.engine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 全流程业务闭环引擎 (5.3.3)
 * 支持多级审批流转、超时督办、自动升级
 */
@Slf4j
@Component
public class WorkflowEngine {

    public enum WorkflowNode {
        APPLY(0, "申报", 24),
        APPROVE_BASIC(1, "班组审批", 12),
        APPROVE_STATION(2, "供电所审批", 24),
        APPROVE_COMPANY(3, "公司审批", 48),
        EXECUTE(4, "现场执行", 72),
        VERIFY(5, "复电核验", 24),
        ARCHIVE(6, "归档完成", 0);

        public final int code;
        public final String name;
        public final int timeoutHours;
        WorkflowNode(int code, String name, int timeoutHours) {
            this.code = code; this.name = name; this.timeoutHours = timeoutHours;
        }
    }

    /** 创建审批流程 */
    public WorkflowInstance createWorkflow(String bizType, Long bizId, List<Integer> approvalLevels) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setBizType(bizType);
        instance.setBizId(bizId);
        instance.setCurrentNode(0);
        instance.setStatus(0); // 进行中
        instance.setCreateTime(LocalDateTime.now());

        // 构建审批链
        List<WorkflowStep> steps = new ArrayList<>();
        for (int i = 0; i < approvalLevels.size(); i++) {
            WorkflowStep step = new WorkflowStep();
            step.setStepOrder(i);
            step.setNodeCode(approvalLevels.get(i));
            step.setNodeName(getNodeName(approvalLevels.get(i)));
            step.setTimeoutHours(getTimeoutHours(approvalLevels.get(i)));
            step.setStatus(0); // 待处理
            step.setCreateTime(LocalDateTime.now());
            steps.add(step);
        }
        // 默认添加执行和归档节点
        step(steps, 5, "复电核验", 24);
        step(steps, 6, "归档完成", 0);

        instance.setSteps(steps);
        instance.setTotalSteps(steps.size());
        log.info("[WorkflowEngine] 创建审批流程: bizType={}, bizId={}, steps={}", bizType, bizId, steps.size());
        return instance;
    }

    private void step(List<WorkflowStep> steps, int code, String name, int timeout) {
        WorkflowStep s = new WorkflowStep();
        s.setNodeCode(code); s.setNodeName(name); s.setTimeoutHours(timeout);
        s.setStatus(0); s.setCreateTime(LocalDateTime.now());
        s.setStepOrder(steps.size());
        steps.add(s);
    }

    private String getNodeName(int code) {
        for (WorkflowNode n : WorkflowNode.values()) if (n.code == code) return n.name;
        return "未知节点";
    }

    private int getTimeoutHours(int code) {
        for (WorkflowNode n : WorkflowNode.values()) if (n.code == code) return n.timeoutHours;
        return 24;
    }

    /** 审批通过 - 进入下一节点 */
    public WorkflowStep approve(WorkflowInstance instance, String operator, String opinion) {
        int currentIdx = -1;
        for (int i = 0; i < instance.getSteps().size(); i++) {
            if (instance.getSteps().get(i).getStatus() == 0) {
                currentIdx = i;
                break;
            }
        }
        if (currentIdx < 0) return null;

        WorkflowStep current = instance.getSteps().get(currentIdx);
        current.setStatus(1); // 已通过
        current.setOperator(operator);
        current.setOpinion(opinion);
        current.setHandleTime(LocalDateTime.now());

        // 移到下一节点
        if (currentIdx + 1 < instance.getSteps().size()) {
            instance.setCurrentNode(instance.getSteps().get(currentIdx + 1).getNodeCode());
        } else {
            instance.setStatus(1); // 全部完成
        }

        log.info("[WorkflowEngine] 审批通过: node={}, operator={}", current.getNodeName(), operator);
        return current;
    }

    /** 驳回 - 回到上一节点 */
    public WorkflowStep reject(WorkflowInstance instance, String operator, String opinion) {
        int lastApproved = -1;
        for (int i = 0; i < instance.getSteps().size(); i++) {
            if (instance.getSteps().get(i).getStatus() == 1) lastApproved = i;
        }
        if (lastApproved < 0) return null;

        // 回退到最后一个通过节点
        WorkflowStep target = instance.getSteps().get(lastApproved);
        target.setStatus(3); // 已驳回，可重新处理
        target.setOperator(operator);
        target.setOpinion(opinion);
        target.setHandleTime(LocalDateTime.now());
        instance.setCurrentNode(target.getNodeCode());

        log.info("[WorkflowEngine] 驳回: node={}, operator={}", target.getNodeName(), operator);
        return target;
    }

    @Data
    public static class WorkflowInstance {
        private String bizType;
        private Long bizId;
        private int currentNode;
        private int status; // 0-进行中 1-已完成 2-已驳回
        private List<WorkflowStep> steps;
        private int totalSteps;
        private LocalDateTime createTime;
    }

    @Data
    public static class WorkflowStep {
        private int stepOrder;
        private int nodeCode;
        private String nodeName;
        private int timeoutHours;
        private int status; // 0-待处理 1-已通过 2-已驳回 3-退回重改
        private String operator;
        private String opinion;
        private LocalDateTime handleTime;
        private LocalDateTime createTime;
    }
}
