package com.olive.system.domain;

import com.olive.framework.web.system.BaseEntity;
import org.babyfish.jimmer.sql.*;

/**
 * @author jhlz
 * @version x.x.x
 */
@Entity
@Table(name = "sys_config")
public interface SysConfig extends BaseEntity {
    /**
     * 参数主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long configId();

    /**
     * 参数名称
     */
    String configName();

    /**
     * 参数键名
     */
    String configKey();

    /**
     * 参数键值
     */
    String configValue();

    /**
     * 系统内置（Y是 N否）
     */
    String configType();
}
