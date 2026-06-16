package com.powerreliability.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAddRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Long deptId;
    private String post;
    private String avatar;
    private Integer status;
    private List<Long> roleIds;
}
