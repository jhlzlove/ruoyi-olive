package com.olive.service;

import com.olive.model.*;
import com.olive.model.dto.SysRoleSearch;
import com.olive.model.exception.SecurityException;
import com.olive.model.exception.SysRoleException;
import com.olive.model.record.PageQuery;
import com.olive.service.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysRoleService {
    private final JSqlClient sqlClient;
    private final SysPermissionService permissionService;
    private final SysUserService userService;
    private final TokenService tokenService;
    private final SysRoleTable table = SysRoleTable.$;

    public Page<SysRole> page(SysRoleSearch search, PageQuery pageQuery) {
        PageQuery page = PageQuery.create(pageQuery);
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.createTime().geIf(page.beginTime()))
                .where(table.createTime().leIf(page.endTime()))
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public List<SysRole> list() {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .select(table)
                .execute();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean add(SysRole role) {
        if (checkRoleNameUnique(role)) {
            throw SysRoleException.roleNameExist("新增角色失败，角色名称已存在", role.roleName());
        }
        else if (checkRoleKeyUnique(role)) {
            throw SysRoleException.roleKeyExist("新增角色失败，角色权限已存在", role.roleKey());
        }
        return sqlClient.getEntities().save(role).getTotalAffectedRowCount() > 0;
    }

    public void checkRoleAllowed(SysRole role) {
        if (ImmutableObjects.isLoaded(role, SysRoleProps.ROLE_ID)
                && Objects.equals(role.roleId(), 1)) {
            throw SecurityException.notAllowOperateSuperAdmin("不允许操作超级管理员角色");
        }
    }

    public void checkRoleDataScope(long roleId) {
        Long userId = SecurityUtils.getUserId();
        if (!(userId != null && 1L == userId)) {
            List<SysRole> roles = sqlClient.createQuery(table)
                    .where(table.roleId().eq(roleId))
                    .where(table.delFlag().eq("0"))
                    .orderBy(table.roleSort())
                    .select(table)
                    .execute();
            if (CollectionUtils.isEmpty(roles)) {
                throw SecurityException.notAllowAccessUserData("没有权限访问角色数据！");
            }
        }
    }

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return  int
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysRole role) {
        checkRoleAllowed(role);
        checkRoleDataScope(role.roleId());
        if (checkRoleNameUnique(role)) {
            throw SysRoleException.roleNameExist("修改角色失败，角色名称已存在", role.roleName());
        } else if (checkRoleKeyUnique(role)) {
            throw SysRoleException.roleKeyExist("修改角色失败，角色权限已存在", role.roleKey());
        }
        boolean b = sqlClient.getEntities()
                // 中间表采用替换模式进行脱钩
                .save(role, SaveMode.UPDATE_ONLY,AssociatedSaveMode.REPLACE)
                .getTotalAffectedRowCount() > 0;
        if (b) {
            // 更新缓存用户权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (Objects.nonNull(loginUser.getUser()) && !(loginUser.getUser().userId() ==1L)) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                // loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                String userName = loginUser.getUser().userName();
                loginUser.setUser(userService.selectUserByUserName(userName));
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                tokenService.setLoginUser(loginUser);
            }
        }
        return b;
    }

    public void authDataScope(SysRole role) {
        sqlClient.getEntities().save(role);
        // 修改角色信息
        // roleMapper.updateRole(role);
        // 删除角色与部门关联
        // roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 新增角色和部门信息（数据权限）
        // return insertRoleDept(role);
    }

    public void deleteRoleByIds(List<Long> roleIds) {
        sqlClient.deleteByIds(SysRole.class, roleIds);
    }

    public SysRole info(long roleId) {
        return sqlClient.findById(SysRole.class, roleId);
    }

    /**
     * @param roleId  授权角色
     * @param userIds 授权用户列表
     * @return 结果
     */
    public boolean insertAuthUsers(Long roleId, Long[] userIds) {
        return sqlClient.getAssociations(SysUserProps.ROLES)
                .saveAll(List.of(userIds), List.of(roleId)) > 0;
    }

    /**
     * 取消授权
     *
     * @param user
     * @return
     */
    public boolean deleteAuthUser(SysUser user) {
        return sqlClient.getAssociations(SysUserProps.ROLES)
                .deleteAll(List.of(user.userId()), List.of(user.roleIds())) > 0;
    }

    public boolean deleteAuthUsers(Long roleId, Long[] userIds) {
        return sqlClient.getAssociations(SysUserProps.ROLES)
                .deleteAll(List.of(userIds), List.of(roleId)) > 0;
    }

    public Collection<String> selectRolePermissionByUserId(Long userId) {
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

    public List<SysRole> selectRolesByUserId(long userId) {
        SysUser user = sqlClient.findById(SysUser.class, userId);
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.users(u -> u.userId().eqIf(userId)))
                .where(table.users(u -> u.deptId().eqIf(user.deptId())))
                .select(table)
                .execute();

    }

    private boolean checkRoleKeyUnique(SysRole role) {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.roleId().ne(role.roleId()))
                .where(table.roleKey().eq(role.roleKey()))
                .exists();
    }

    private boolean checkRoleNameUnique(SysRole role) {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.roleId().ne(role.roleId()))
                .where(table.roleName().eq(role.roleName()))
                .exists();
    }
}
