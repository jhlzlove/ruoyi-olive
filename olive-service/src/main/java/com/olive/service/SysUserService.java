package com.olive.service;

import com.olive.model.*;
import com.olive.model.dto.SysUserSearch;
import com.olive.model.exception.SecurityException;
import com.olive.model.exception.SysUserException;
import com.olive.model.record.PageQuery;
import com.olive.model.record.SysUserCondition;
import com.olive.service.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.DraftObjects;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.query.ConfigurableSubQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jhlz
 * @version x.x.x
 */
@Service
@AllArgsConstructor
public class SysUserService {
    private final JSqlClient sqlClient;

    private final SysUserTable table = SysUserTable.$;

    public Page<SysUser> page(SysUserSearch search, Long deptId, PageQuery pageQuery) {
        PageQuery page = PageQuery.create(pageQuery);
        // AND (u.dept_id = #{deptId} OR u.dept_id IN ( SELECT t.dept_id FROM sys_dept t WHERE find_in_set(#{deptId}, ancestors) ))
        ConfigurableSubQuery<Long> subQuery = null;
        if (Objects.nonNull(deptId)) {
            SysDeptTable deptTable = SysDeptTable.$;
            subQuery = sqlClient.createSubQuery(deptTable)
                    .where(Predicate.sql("find_in_set(%v, %e)", it -> {
                        it.expression(deptTable.ancestors()).value(deptId);
                    }))
                    .select(table.deptId());
        }

        return sqlClient.createQuery(table)
                .where(search)
                .where(Predicate.or(table.deptId().inIf(subQuery), table.deptId().eq(deptId)))
                .where(table.delFlag().eq("0"))
                .select(table.fetch(
                        Fetchers.SYS_USER_FETCHER.allScalarFields()
                                .dept(Fetchers.SYS_DEPT_FETCHER.deptName())
                ))
                .fetchPage(page.pageNum() - 1, page.pageSize());
    }

    public Page<SysUser> selectAllocatedList(SysUserCondition user) {
        Long deptId = user.deptId();
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.dept().deptId().eqIf(deptId))
                .where(table.roles(role -> role.roleId().inIf(user.roleIds())))
                .where(table.userName().eqIf(user.userName()))
                .where(table.phonenumber().eqIf(user.phonenumber()))
                .select(table.fetch(Fetchers.SYS_USER_FETCHER
                        .deptId()
                        .userName()
                        .nickName()
                        .email()
                        .phonenumber()
                        .status()
                        .createTime()
                ))
                .fetchPage(0, 10);
    }

    /**
     * 未分配角色列表
     *
     * @param user 搜索条件
     * @return
     */
    public Page<SysUser> selectUnallocatedList(SysUserCondition user) {

        // select distinct u.user_id, u.dept_id, u.user_name, u.nick_name, u.email, u.phonenumber, u.status, u.create_time
        // from sys_user u
        // left join sys_dept d on u.dept_id = d.dept_id
        // left join sys_user_role ur on u.user_id = ur.user_id
        // left join sys_role r on r.role_id = ur.role_id
        // where u.del_flag = '0' and (r.role_id != #{roleId} or r.role_id IS NULL)
        // and u.user_id not in (select u.user_id from sys_user u inner join sys_user_role ur on u.user_id = ur.user_id and ur.role_id = #{roleId})
        // <if test="userName != null and userName != ''">
        //         AND u.user_name like concat('%', #{userName}, '%')
        // </if>
        // <if test="phonenumber != null and phonenumber != ''">
        //         AND u.phonenumber like concat('%', #{phonenumber}, '%')
        // </if>
        Long deptId = user.deptId();
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.roles(role -> role.roleId().notIn(user.roleIds())))
                .where(table.roles(role -> role.roleId().isNotNull()))
                .where(table.dept().deptId().eqIf(deptId))
                .where(table.roles(role -> role.roleId().inIf(user.roleIds())))
                .where(table.userName().eqIf(user.userName()))
                .where(table.phonenumber().eqIf(user.phonenumber()))
                .select(table.fetch(Fetchers.SYS_USER_FETCHER
                        .deptId()
                        .userName()
                        .nickName()
                        .email()
                        .phonenumber()
                        .status()
                        .createTime()
                ))
                .fetchPage(0, 10);
    }

    public SysUser info(Long userId) {
        return sqlClient.createQuery(table)
                .where(table.userId().eq(userId))
                .select(table.fetch(Fetchers.SYS_USER_FETCHER.allScalarFields().roleIds()))
                .fetchOne();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(SysUser user) {
        SysUser res = Immutables.createSysUser(user, draft -> {
            draft.setPassword(SecurityUtils.encryptPassword(user.password()));
        });
        return sqlClient.getEntities().save(res).getTotalAffectedRowCount() > 0;
    }

    public String selectUserPostGroup(String username) {
        SysPostTable postTable = SysPostTable.$;
        List<SysPost> list = sqlClient.createQuery(postTable)
                .where(postTable.userList(u -> u.userName().eq(username)))
                .select(postTable.fetch(
                        Fetchers.SYS_POST_FETCHER
                                .postName().postCode()
                ))
                .execute();

        if (CollectionUtils.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return list.stream().map(SysPost::postName).collect(Collectors.joining(","));
    }

    public String selectUserRoleGroup(String username) {
        SysRoleTable roleTable = SysRoleTable.$;
        List<SysRole> roles = sqlClient.createQuery(roleTable)
                .where(roleTable.delFlag().eq("0"))
                .where(roleTable.users(r -> r.userName().eq(username)))
                .select(roleTable)
                .execute();
        if (CollectionUtils.isEmpty(roles)) {
            return StringUtils.EMPTY;
        }
        return roles.stream().map(SysRole::roleName).collect(Collectors.joining(","));
    }

    public int resetUserPwd(String userName, String newPassword) {
        return sqlClient.createUpdate(table)
                .set(table.userName(), newPassword)
                .where(table.userName().eq(userName))
                .execute();
    }

    public int updateUserAvatar(String username, String avatar) {
        return sqlClient.createUpdate(table)
                .set(table.avatar(), avatar)
                .where(table.userName().eq(username))
                .execute();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateUserProfile(SysUser user) {
        return sqlClient.getEntities().save(user).getTotalAffectedRowCount();
    }


    public SysUser selectUserByUserName(String username) {
        return sqlClient.createQuery(table)
                .where(table.userName().eq(username))
                .select(
                        table.fetch(
                                Fetchers.SYS_USER_FETCHER.allScalarFields()
                                        .roles(Fetchers.SYS_ROLE_FETCHER.allScalarFields())
                                        .dept(Fetchers.SYS_DEPT_FETCHER.allScalarFields())
                        )
                )
                .fetchOne();
    }



    @Transactional(rollbackFor = Exception.class)
    public boolean add(SysUser user) {
        if (!checkUserNameUnique(user.userName())) {
            throw SysUserException.usernameExist("新增用户失败，登录账号已存在", user.userName());
        } else if (StringUtils.isNotEmpty(user.phonenumber()) && !checkPhoneUnique(user)) {
            throw SysUserException.phoneExist("新增用户失败，手机号码已存在", user.phonenumber());
        } else if (StringUtils.isNotEmpty(user.email()) && !checkEmailUnique(user)) {
            throw SysUserException.emailExist("新增用户失败，邮箱账号已存在", user.email());
        }
        SysUser target = Immutables.createSysUser(user, draft -> {
            DraftObjects.set(draft, SysUserProps.PASSWORD, SecurityUtils.encryptPassword(user.password()));
        });
        return sqlClient.getEntities().save(target).getTotalAffectedRowCount() > 0;
    }

    public boolean update(SysUser user) {
        checkUserAllowed(user);
        checkUserDataScope(user.userId());
        if (!checkUserNameUnique(user.userName())) {
            throw SysUserException.usernameExist("修改用户失败，登录账号已存在", user.userName());
        } else if (StringUtils.isNotEmpty(user.phonenumber()) && !checkPhoneUnique(user)) {
            throw SysUserException.phoneExist("修改用户失败，手机号码已存在", user.phonenumber());
        } else if (StringUtils.isNotEmpty(user.email()) && !checkEmailUnique(user)) {
            throw SysUserException.emailExist("修改用户失败，邮箱账号已存在", user.email());
        }
        return sqlClient.getEntities().save(user).getTotalAffectedRowCount() > 0;
    }

    public boolean delete(List<Long> userIds) {
        if (ArrayUtils.contains(userIds.toArray(), SecurityUtils.getUserId())) {
            throw SecurityException.notAllowAccessUserData("当前用户不能删除");
        }
        return sqlClient.deleteByIds(SysUser.class, userIds).getTotalAffectedRowCount() > 0;
    }

    public void insertUserAuth(Long userId, Long[] roleIds) {
        sqlClient.getAssociations(SysUserProps.ROLES)
                .insertAll(List.of(userId), List.of(roleIds));
    }

    public void checkUserAllowed(SysUser user) {
        if (1L == user.userId()) {
            throw SecurityException.notAllowOperateSuperAdmin("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验数据权限
     */
    public void checkUserDataScope(Long userId) {
        Long id = SecurityUtils.getUserId();
        if (!(Objects.nonNull(id) && 1L == id)) {

            List<SysUser> result = sqlClient.createQuery(table)
                    .where(table.delFlag().eq("0"))
                    .where(table.userId().eq(userId))
                    .select(table)
                    .execute();
            if (CollectionUtils.isEmpty(result)) {
                throw SecurityException.notAllowAccessUserData("没有权限访问用户数据！");
            }
        }
    }

    /**
     * 校验手机号是否唯一
     *
     * @param user 用户信息
     * @return true ？ 唯一 ：不唯一
     */
    public boolean checkPhoneUnique(SysUser user) {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.userId().ne(user.userId()))
                .where(table.phonenumber().eq(user.phonenumber()))
                .exists();
    }

    public boolean checkEmailUnique(SysUser user) {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.userId().ne(user.userId()))
                .where(table.email().eq(user.email()))
                .exists();
    }
    public boolean checkUserNameUnique(String username) {
        return sqlClient.createQuery(table)
                .where(table.userName().eq(username))
                .exists();
    }
}
