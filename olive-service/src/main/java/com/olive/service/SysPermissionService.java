package com.olive.service;

import com.olive.model.SysRole;
import com.olive.model.SysRoleTable;
import com.olive.model.SysUser;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 用户权限处理
 *
 * @author ruoyi
 */
@Component
@AllArgsConstructor
public class SysPermissionService {
    private final JSqlClient sqlClient;

    private final SysMenuService menuService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUser user) {
        Set<String> roles = new HashSet<String>();
        // 管理员拥有所有权限
        if (Objects.nonNull(user) && 1L == user.userId()) {
            roles.add("admin");
        } else {
            roles.addAll(selectRolePermissionByUserId(user.userId()));
        }
        return roles;
    }

    public Collection<String> selectRolePermissionByUserId(Long userId) {
        SysRoleTable table = SysRoleTable.$;
        List<SysRole> roles = sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.users(u -> u.userId().eq(userId)))
                .select(table)
                .execute();
        Set<String> permsSet = new HashSet<>();

        for (SysRole perm : roles) {
            if (Objects.nonNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.roleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 获取菜单数据权限
     *
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUser user) {
        Set<String> perms = new HashSet<String>();
        // 管理员拥有所有权限
        if (Objects.nonNull(user) && 1L == user.userId()) {
            perms.add("*:*:*");
        } else {
            List<SysRole> roles = user.roles();
            if (!CollectionUtils.isEmpty(roles)) {
                // 多角色设置permissions属性，以便数据权限匹配权限
                for (SysRole role : roles) {
                    Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.roleId());

                    // role.setPermissions(rolePerms);
                    perms.addAll(rolePerms);
                }
            } else {
                perms.addAll(menuService.selectMenuPermsByUserId(user.userId()));
            }
        }
        return perms;
    }
}
