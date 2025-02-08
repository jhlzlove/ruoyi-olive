package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.PageQuery;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.web.system.*;
import com.olive.framework.web.system.dto.SysRoleSearch;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysRoleService {
    private final JSqlClient sqlClient;
    private final SysRoleTable table = SysRoleTable.$;

    public Page<SysRole> page(SysRoleSearch search, PageQuery page) {
        LocalDateTime begin = null;
        LocalDateTime end = null;
        if (Objects.nonNull(page) && Objects.nonNull(page.beginTime()) && Objects.nonNull(page.endTime())) {
            begin = LocalDateUtil.dateStrToDateTime(page.beginTime());
            end = LocalDateUtil.dateStrToDateTime(page.endTime());
        }
        return sqlClient.createQuery(table)
                .where(search)
                .where(table.createTime().geIf(begin))
                .where(table.createTime().leIf(end))
                .select(table)
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public boolean checkRoleNameUnique(SysRole role) {
        long roleId = ImmutableObjects.isLoaded(role, SysRoleProps.ROLE_ID) ? role.roleId() : -1L;
        SysRole info = sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.roleName().eq(role.roleName()))
                .select(table)
                .fetchOneOrNull();
        if (StringUtils.isNotNull(info) && info.roleId() != roleId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role) {
        return sqlClient.insert(role).getTotalAffectedRowCount();
    }

    public void checkRoleAllowed(SysRole role) {
        if (ImmutableObjects.isLoaded(role, SysRoleProps.ROLE_ID)
                && Objects.equals(role.roleId(), 1)) {
            throw new ServiceException("不允许操作超级管理员角色");
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
                throw new ServiceException("没有权限访问角色数据！");
            }
        }
    }

    /**
     * 更新角色信息
     * @param role 角色信息
     * @return  int
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role) {
        return sqlClient
                // 中间表采用替换模式进行脱钩
                .update(role, AssociatedSaveMode.REPLACE)
                .getTotalAffectedRowCount();
    }

    public void authDataScope(SysRole role) {
        sqlClient.update(role);
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

    public List<SysRole> list() {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .select(table)
                .execute();
    }

    public boolean checkRoleKeyUnique(SysRole role) {
        long roleId = ImmutableObjects.isLoaded(role, SysRoleProps.ROLE_ID) ? role.roleId() : -1L;
        SysRole info = sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.roleKey().eq(role.roleKey()))
                .select(table)
                .fetchOneOrNull();
        // SysRole info = roleMapper.checkRoleKeyUnique(role.roleKey());
        if (Objects.nonNull(info) && info.roleId() != roleId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    public SysRole selectRoleById(long roleId) {
        return sqlClient.findById(SysRole.class, roleId);
    }

    public List<SysRole> selectRoleAll() {
        return sqlClient.getEntities().findAll(SysRole.class);
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
            if (StringUtils.isNotNull(perm)) {
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
}
