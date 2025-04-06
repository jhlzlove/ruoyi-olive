package com.olive.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 菜单权限表 sys_menu
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_menu")
public interface SysMenu extends BaseEntity {

    /**
     * 菜单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long menuId();

    /**
     * 菜单名称
     */
    String menuName();

    /**
     * 父菜单ID
     */
    @IdView
    @Nullable
    Long parentId();

    /**
     * 显示顺序
     */
    @Nullable
    Integer orderNum();

    /**
     * 路由地址
     */
    @Nullable
    String path();

    /**
     * 组件路径
     */
    @Nullable
    String component();

    /**
     * 路由参数
     */
    @Nullable
    String query();

    /**
     * 路由名称，默认和路由地址相同的驼峰格式（注意：因为vue3版本的router会删除名称相同路由，为避免名字的冲突，特殊情况可以自定义）
     */
    @Nullable
    String routeName();

    /**
     * 是否为外链（0是 1否）
     */
    @Nullable
    @Column(name = "is_frame")
    @JsonAlias("isFrame")
    String frameFlag();

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    @Nullable
    @Column(name = "is_cache")
    @JsonAlias("isCache")
    String cacheFlag();

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    @Nullable
    String menuType();

    /**
     * 显示状态（0显示 1隐藏）
     */
    @Nullable
    String visible();

    /**
     * 菜单状态（0正常 1停用）
     */
    @Nullable
    String status();

    /**
     * 权限字符串
     */
    @Nullable
    String perms();

    /**
     * 菜单图标
     */
    @Nullable
    String icon();

    @ManyToMany
    @JoinTable(
            name = "sys_role_menu",
            joinColumnName = "menu_id",
            inverseJoinColumnName = "role_id"
    )
    List<SysRole> roles();

    @IdView("roles")
    List<Long> roleIds();

    /**
     * 自关联，构造树结构
     */
    @Nullable
    @ManyToOne
    SysMenu parent();

    /**
     * 子菜单
     */
    @OneToMany(mappedBy = "parent")
    List<SysMenu> children();

}
