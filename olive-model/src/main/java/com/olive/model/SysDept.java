package com.olive.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 部门表 sys_dept
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_dept")
public interface SysDept extends BaseEntity {

    /**
     * 部门ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long deptId();

    /**
     * 父部门ID
     */
    @IdView("parent")
    Long parentId();

    /**
     * 祖级列表
     */
    @Nullable
    String ancestors();

    /**
     * 部门名称
     */
    // @Key
    String deptName();

    /**
     * 显示顺序
     */
    @Nullable
    Integer orderNum();

    /**
     * 负责人
     */
    @Nullable
    String leader();

    /**
     * 联系电话
     */
    @Nullable
    String phone();

    /**
     * 邮箱
     */
    @Nullable
    String email();

    /**
     * 部门状态:0正常,1停用
     */
    @Nullable
    String status();

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @Nullable
    String delFlag();

    /**
     * 父部门信息
     */
    @Nullable
    @ManyToOne
    SysDept parent();

    /**
     * 子部门列表
     */
    @OneToMany(mappedBy = "parent")
    List<SysDept> children();

    /**
     * 关联用户列表
     */
    @OneToMany(mappedBy = "dept")
    List<SysUser> users();

    /**
     * 关联角色列表
     */
    @ManyToMany
    @JoinTable(
            name = "sys_role_dept",
            joinColumnName = "dept_id",
            inverseJoinColumnName = "role_id"
    )
    List<SysRole> roles();

    // @Nullable
    // @Transient(ref = "deptNameResolver")
    // String parentName();
}
