package com.powerreliability.system.dto;

import lombok.Data;

@Data
public class RoleAddRequest {
    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private Integer status;
}
