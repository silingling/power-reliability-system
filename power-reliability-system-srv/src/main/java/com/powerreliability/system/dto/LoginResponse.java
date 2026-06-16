package com.powerreliability.system.dto;

import com.powerreliability.system.entity.SysMenu;
import com.powerreliability.system.entity.SysUser;
import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private SysUser userInfo;
    private List<String> permissions;
    private List<SysMenu> menus;
}
