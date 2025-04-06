package com.olive.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 角色表 sys_role
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_role")
public interface SysRole extends BaseEntity {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long roleId();

    /**
     * 角色名称
     */
    String roleName();

    /**
     * 角色权限
     */
    @Key
    String roleKey();

    /**
     * 角色排序
     */
    Integer roleSort();

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
     */
    String dataScope();

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    boolean menuCheckStrictly();

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    boolean deptCheckStrictly();

    /**
     * 角色状态（0正常 1停用）
     */
    String status();

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    String delFlag();

    /**
     * 备注
     */
    @Nullable
    String remark();

    /**
     * 菜单组
     */
    @IdView("menus")
    List<Long> menuIds();

    /**
     * 部门组（数据权限）
     */
    @IdView("depts")
    List<Long> deptIds();

    /**
     * 关联部门列表
     */
    @ManyToMany(mappedBy = "roles")
    List<SysDept> depts();

    /**
     * 关联菜单列表
     */
    @ManyToMany(mappedBy = "roles")
    List<SysMenu> menus();

    /**
     * 用户列表
     */
    @ManyToMany(mappedBy = "roles")
    List<SysUser> users();

}
