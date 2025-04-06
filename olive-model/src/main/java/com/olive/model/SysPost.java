package com.olive.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 岗位表 sys_post
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_post")
public interface SysPost extends BaseEntity {

    /**
     * 岗位序号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long postId();

    /**
     * 岗位编码
     */
    String postCode();

    /**
     * 岗位名称
     */
    String postName();

    /**
     * 岗位排序
     */
    Integer postSort();

    @Nullable
    String remark();

    /**
     * 状态（0正常 1停用）
     */
    String status();

    /**
     * 用户列表
     */
    @ManyToMany(mappedBy = "posts")
    List<SysUser> userList();
}
