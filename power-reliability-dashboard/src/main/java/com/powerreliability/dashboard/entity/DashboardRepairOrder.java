package com.powerreliability.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class DashboardRepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long eventId;

    private String eventNo;

    private LocalDateTime dispatchTime;

    private LocalDateTime arriveTime;

    private String repairTeam;

    private LocalDateTime repairCompleteTime;

    private LocalDateTime recoveryVerifyTime;

    private Integer orderStatus;

    @TableLogic
    private Integer isDeleted;
}
