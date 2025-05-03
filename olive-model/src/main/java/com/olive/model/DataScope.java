package com.olive.model;

import org.babyfish.jimmer.sql.ManyToOne;
import org.babyfish.jimmer.sql.MappedSuperclass;
import org.jetbrains.annotations.Nullable;

/**
 * @author jhlz
 * @version x.x.x
 */
@MappedSuperclass
public interface DataScope {

    @ManyToOne
    @Nullable
    SysUser sysUser();

    @ManyToOne
    @Nullable
    SysDept sysDept();
}
