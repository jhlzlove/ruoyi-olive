package com.olive.system.service;

import com.olive.common.utils.LocalDateUtil;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.record.PageQuery;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.record.SysUserCondition;
import com.olive.framework.web.system.*;
import com.olive.framework.web.system.dto.SysUserSearch;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.Page;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.query.ConfigurableSubQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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

    public List<SysUser> findAll() {
        return sqlClient.createQuery(table)
                .select(
                        table.fetch(Fetchers.SYS_USER_FETCHER
                                .allScalarFields()
                                .dept(Fetchers.SYS_DEPT_FETCHER.deptName())
                        ))
                .execute();
    }

    public Page<SysUser> page(SysUserSearch search, Long deptId, PageQuery page) {
        LocalDateTime begin = null;
        LocalDateTime end = null;
        if (Objects.nonNull(page) && Objects.nonNull(page.beginTime()) && Objects.nonNull(page.endTime())) {
            begin = LocalDateUtil.dateStrToDateTime(page.beginTime());
            end = LocalDateUtil.dateStrToDateTime(page.endTime());
        }
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
                .where(table.createTime().geIf(begin))
                .where(table.createTime().leIf(end))
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

    public SysUser selectUserById(Long userId) {
        return sqlClient.createQuery(table)
                .where(table.userId().eq(userId))
                .select(table.fetch(Fetchers.SYS_USER_FETCHER.allScalarFields().roleIds()))
                .fetchOne();
    }

    public SysUser selectRoleIdsByUserId(Long userId) {
        return sqlClient.createQuery(table)
                .where(table.userId().eq(userId))
                .select(table.fetch(Fetchers.SYS_USER_FETCHER.roleIds()))
                .fetchOne();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateUserProfile(SysUser user) {
        return sqlClient.update(user).getTotalAffectedRowCount();
    }

    public boolean checkUserNameUnique(String username) {
        return sqlClient.createQuery(table)
                .where(table.userName().eq(username))
                .select(table)
                .fetchOneOrNull() != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean registerUser(SysUser user) {
        SysUser res = Immutables.createSysUser(user, draft -> {
            draft.setPassword(SecurityUtils.encryptPassword(user.password()));
        });
        return sqlClient.insert(res).getTotalAffectedRowCount() > 0;
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
                .select(table)
                .fetchOneOrNull() == null;
    }

    public boolean checkEmailUnique(SysUser user) {
        return sqlClient.createQuery(table)
                .where(table.delFlag().eq("0"))
                .where(table.userId().ne(user.userId()))
                .where(table.email().eq(user.email()))
                .select(table)
                .fetchOneOrNull() == null;
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
                throw new ServiceException("没有权限访问用户数据！");
            }
        }
    }

    public boolean insertUser(SysUser temp) {
        return sqlClient.insert(temp).getTotalAffectedRowCount() > 0;
    }

    public void checkUserAllowed(SysUser user) {
        if (Objects.nonNull(user.userId()) && 1L == user.userId()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    public boolean updateUser(SysUser user) {
        return sqlClient.update(user).getTotalAffectedRowCount() > 0;
    }

    public boolean deleteUserByIds(List<Long> userIds) {
        return sqlClient.deleteByIds(SysUser.class, userIds).getTotalAffectedRowCount() > 0;
    }

    public void insertUserAuth(Long userId, Long[] roleIds) {
        sqlClient.getAssociations(SysUserProps.ROLES)
                .insertAll(List.of(userId), List.of(roleIds));
    }
}
