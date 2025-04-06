package com.olive.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * 登录日志
 *
 * @author jhlz
 * @version x.x.x
 */
@Entity
@Table(name = "sys_login_log")
public interface SysLoginLog {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long infoId();

    /**
     * 用户账号
     */
    @Nullable
    String userName();

    /**
     * 登录状态 0成功 1失败
     */
    @Nullable
    String status();

    /**
     * 登录IP地址
     */
    @Nullable
    String ipaddr();

    /**
     * 登录地点
     */
    @Nullable
    String loginLocation();

    /**
     * 浏览器类型
     */
    @Nullable
    String browser();

    /**
     * 操作系统
     */
    @Nullable
    String os();

    /**
     * 提示消息
     */
    @Nullable
    String msg();

    /**
     * 访问时间
     */
    @Nullable
    LocalDateTime loginTime();
}
