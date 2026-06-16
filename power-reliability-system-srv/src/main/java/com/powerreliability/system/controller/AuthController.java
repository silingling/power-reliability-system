package com.powerreliability.system.controller;

import com.powerreliability.common.auth.JwtUtil;
import com.powerreliability.common.entity.Result;
import com.powerreliability.system.dto.LoginRequest;
import com.powerreliability.system.dto.LoginResponse;
import com.powerreliability.system.entity.SysMenu;
import com.powerreliability.system.entity.SysUser;
import com.powerreliability.system.service.ISysMenuService;
import com.powerreliability.system.service.ISysOperationLogService;
import com.powerreliability.system.service.ISysRoleService;
import com.powerreliability.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/system/auth")
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ISysOperationLogService sysOperationLogService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        SysUser user = sysUserService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        // 获取权限和菜单
        List<String> permissions = sysRoleService.getUserPermissions(user.getId());
        List<SysMenu> allMenus = sysMenuService.list();
        List<SysMenu> menuTree = sysMenuService.buildTree(allMenus);

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), permissions);

        // 构造响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        user.setPassword(null); // 不返回密码
        response.setUserInfo(user);
        response.setPermissions(permissions);
        response.setMenus(menuTree);

        // 记录登录日志
        sysOperationLogService.record(user.getId(), "系统管理", "登录",
                user.getId().toString(), request.getUsername(), "成功", httpRequest.getRemoteAddr());

        return Result.ok(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        // 实际token失效可由前端清除或Redis黑名单处理
        return Result.ok();
    }
}
