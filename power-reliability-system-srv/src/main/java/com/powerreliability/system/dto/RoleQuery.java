package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class RoleQuery {
    private String keyword;
    private Integer status;
    private int page = 1;
    private int pageSize = 20;
}
