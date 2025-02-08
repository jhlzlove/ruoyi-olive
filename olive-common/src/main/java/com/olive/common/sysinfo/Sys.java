package com.olive.common.sysinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统相关信息
 *
 * @author ruoyi
 */
public record Sys(
        /* 服务器名称 */
        String computerName,

        /* 服务器Ip */
        String computerIp,

        /*  项目路径 */
        String userDir,

        /* 操作系统 */
        String osName,

        /* 系统架构 */
        String osArch
) {

    public static Sys init() {
        return new Sys(
                getHostName(), getHostIp(),
                System.getProperties().getProperty("user.dir"),
                System.getProperties().getProperty("os.name"),
                System.getProperties().getProperty("os.arch")
        );
    }

    /**
     * 获取IP地址
     *
     * @return 本地IP地址
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    /**
     * 获取主机名
     *
     * @return 本地主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "未知";
    }
}
