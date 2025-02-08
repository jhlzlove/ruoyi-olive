package com.olive.framework.web.system;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 用户对象 sys_user
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_user")
public interface SysUser extends BaseEntity {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long userId();

    /**
     * 部门ID
     */
    @IdView
    @Nullable
    Long deptId();

    /**
     * 用户账号
     */
    String userName();

    /**
     * 用户昵称
     */
    String nickName();

    /**
     * 用户邮箱
     */
    @Nullable
    String email();

    /**
     * 手机号码
     */
    @Nullable
    String phonenumber();

    /**
     * 用户性别
     */
    @Nullable
    String sex();

    /**
     * 用户头像
     */
    @Nullable
    String avatar();

    /**
     * 密码
     */
    // @Nullable
    String password();

    /**
     * 帐号状态（0正常 1停用）
     */
    String status();

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    String delFlag();

    /**
     * 最后登录IP
     */
    @Nullable
    String loginIp();

    /**
     * 最后登录时间
     */
    @Nullable
    ZonedDateTime loginDate();

    /**
     * 部门对象
     */
    @Nullable
    @ManyToOne
    SysDept dept();

    /**
     * 角色对象
     */
    @ManyToMany
    @JoinTable(
            name = "sys_user_role",
            joinColumnName = "user_id",
            inverseJoinColumnName = "role_id"
    )
    List<SysRole> roles();

    /**
     * 岗位对象
     */
    @ManyToMany
    @JoinTable(
            name = "sys_user_post",
            joinColumnName = "user_id",
            inverseJoinColumnName = "post_id"
    )
    List<SysPost> posts();

    /**
     * 角色组
     */
    @IdView("roles")
    List<Long> roleIds();

    /**
     * 岗位组
     */
    @IdView("posts")
    List<Long> postIds();
}
