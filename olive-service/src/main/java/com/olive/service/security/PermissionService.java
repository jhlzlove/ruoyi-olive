package com.olive.service.security;

import com.olive.model.LoginUser;
import com.olive.model.SysRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;

/**
 * RuoYi首创 自定义权限实现，ss取自SpringSecurity首字母
 * Geek改进 将自定义权限实现形成规范，提高扩展性
 *
 * @author ruoyi&&Dftre
 */
@Service("ss")
public class PermissionService implements IPermissionService {
    /**
     * 所有权限标识
     */
    private static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    private static final String SUPER_ADMIN = "admin";

    private static final String ROLE_DELIMETER = ",";

    private static final String PERMISSION_DELIMETER = ",";

    /**
     * 验证用户是否具备某权限
     *
     * @param permission 权限字符串
     * @return 用户是否具备某权限
     */
    public boolean hasPermi(String permission) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(permission)) {
            return false;
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        PermissionContextHolder.setContext(permission);
        return hasPermissions(loginUser.getPermissions(), permission);
    }

    /**
     * 验证用户是否具有以下任意一个权限
     *
     * @param permissions 以 PERMISSION_NAMES_DELIMETER 为分隔符的权限列表
     * @return 用户是否具有以下任意一个权限
     */
    public boolean hasAnyPermi(String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            return false;
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getPermissions())) {
            return false;
        }
        PermissionContextHolder.setContext(permissions);
        Set<String> authorities = loginUser.getPermissions();
        for (String permission : permissions.split(PERMISSION_DELIMETER)) {
            if (hasPermissions(authorities, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有某个角色
     *
     * @param role 角色字符串
     * @return 用户是否具备某角色
     */
    public boolean hasRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return false;
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().roles())) {
            return false;
        }
        for (SysRole sysRole : loginUser.getUser().roles()) {
            String roleKey = sysRole.roleKey();
            if (SUPER_ADMIN.equals(roleKey) || roleKey.equals(StringUtils.trim(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否具有以下任意一个角色
     *
     * @param roles 以 ROLE_NAMES_DELIMETER 为分隔符的角色列表
     * @return 用户是否具有以下任意一个角色
     */
    public boolean hasAnyRoles(String roles) {
        if (StringUtils.isEmpty(roles)) {
            return false;
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getUser().roles())) {
            return false;
        }
        for (String role : roles.split(ROLE_DELIMETER)) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否包含权限
     *
     * @param permissions 权限列表
     * @param permission  权限字符串
     * @return 用户是否具备某权限
     */
    private boolean hasPermissions(Set<String> permissions, String permission) {
        return permissions.contains(ALL_PERMISSION) || permissions.contains(StringUtils.trim(permission));
    }
}
