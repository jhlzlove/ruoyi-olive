package com.olive.system.domain;

import com.olive.framework.web.system.BaseEntity;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 字典类型表 sys_dict_type
 *
 * @author ruoyi
 */
@Entity
@Table(name = "sys_dict_type")
public interface SysDictType extends BaseEntity {

    /**
     * 字典主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long dictId();

    /**
     * 字典名称
     */
    @Key
    String dictName();

    /**
     * 字典类型
     */
    @Key
    String dictType();

    /**
     * 状态（0正常 1停用）
     */
    String status();

    /**
     * 备注
     */
    @Nullable
    String remark();

    @OneToMany(mappedBy = "sysDictType")
    List<SysDictData> dictDatas();
}
