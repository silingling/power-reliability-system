package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class DeptAddRequest {
    private String deptName;
    private Long parentId;
    private Integer deptType;
    private Integer sortOrder;
    private Integer status;
}
