package com.olive.api.system;

import com.olive.framework.util.SecurityUtils;
import com.olive.model.LoginUser;
import com.olive.model.RouterVo;
import com.olive.model.SysUser;
import com.olive.model.constant.AppConstant;
import com.olive.model.record.LoginBody;
import com.olive.service.SysLoginService;
import com.olive.service.SysMenuService;
import com.olive.service.SysPermissionService;
import com.olive.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录验证
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@AllArgsConstructor
public class SysLoginController {
    private final SysMenuService menuService;
    private final SysLoginService loginService;
    private final SysPermissionService permissionService;
    private final TokenService tokenService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginBody loginBody) {
        // 生成令牌
        String token = loginService.login(loginBody.username(),
                loginBody.password(),
                loginBody.code(),
                loginBody.uuid());
        return Map.of(AppConstant.TOKEN, token);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public Map<String, Object> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (!loginUser.getPermissions().equals(permissions)) {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        return Map.of(
                "user", user,
                "roles", roles,
                "permissions", permissions
        );
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public List<RouterVo> getRouters() {
        return menuService.buildRouters();
    }
}
