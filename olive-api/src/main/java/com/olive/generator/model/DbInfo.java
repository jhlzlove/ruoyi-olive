package com.olive.generator.model;

/**
 * @author jhlz
 * @version 0.0.1
 */
public record DbInfo(
        /* 数据库名称 */
        String dbName,
        /* 是否允许连接 */
        String allowConn,
        /* 编码 */
        String encodingType,
        /* 描述 */
        String description
) {
}
