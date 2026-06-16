package com.powerreliability.system.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogQuery {
    private String module;
    private String action;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int page = 1;
    private int pageSize = 20;
}
