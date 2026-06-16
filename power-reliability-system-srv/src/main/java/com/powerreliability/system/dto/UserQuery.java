package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class UserQuery {
    private String keyword;
    private Integer status;
    private Long deptId;
    private int page = 1;
    private int pageSize = 20;
}
