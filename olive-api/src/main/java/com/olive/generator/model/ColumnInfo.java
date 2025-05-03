package com.olive.generator.model;

/**
 * @author jhlz
 * @version 0.0.1
 */
public record ColumnInfo(
        /* 列名称 */
        String columnName,
        /* 类型 */
        String columnType,
        /* 是否可空 */
        String required,
        /* 是否主键 */
        String primary,
        /* 是否外键 */
        String foreign,
        /* 默认值 */
        String defaultValue,
        /* 描述 */
        String description
) {
}
