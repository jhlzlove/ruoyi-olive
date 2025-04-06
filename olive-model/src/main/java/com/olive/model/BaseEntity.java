package com.olive.model;

import org.babyfish.jimmer.sql.MappedSuperclass;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * @author jhlz
 * @version x.x.x
 */
@MappedSuperclass
public interface BaseEntity {

    @Nullable
    LocalDateTime createTime();

    @Nullable
    LocalDateTime updateTime();

    @Nullable
    String createBy();

    @Nullable
    String updateBy();
}
