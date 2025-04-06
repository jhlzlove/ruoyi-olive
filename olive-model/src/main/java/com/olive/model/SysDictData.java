package com.olive.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

/**
 * 字典数据表 sys_dict_data
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_dict_data")
public interface SysDictData extends BaseEntity {

    /**
     * 字典编码
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long dictCode();

    /**
     * 字典排序
     */
    Long dictSort();

    /**
     * 字典标签
     */
    String dictLabel();

    /**
     * 字典键值
     */
    String dictValue();

    /**
     * 字典类型
     */
    String dictType();

    /**
     * 样式属性（其他样式扩展）
     */
    @Nullable
    String cssClass();

    /**
     * 表格字典样式
     */
    String listClass();

    /**
     * 是否默认（Y是 N否）
     */
    @Column(name = "is_default")
    String defaultFlag();

    /**
     * 状态（0正常 1停用）
     */
    String status();

    /**
     * 备注
     */
    @Nullable
    String remark();

    @Nullable
    @ManyToOne
    SysDictType sysDictType();
}
