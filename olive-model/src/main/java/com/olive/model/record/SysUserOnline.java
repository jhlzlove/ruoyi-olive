package com.olive.model.record;

/**
 * 当前在线会话
 *
 * @author ruoyi
 */
public record SysUserOnline(

        /**
         * 会话编号
         */
        String tokenId,

        /**
         * 部门名称
         */
        String deptName,

        /**
         * 用户名称
         */
        String userName,

        /**
         * 登录IP地址
         */
        String ipaddr,

        /**
         * 登录地址
         */
        String loginLocation,

        /**
         * 浏览器类型
         */
        String browser,

        /**
         * 操作系统
         */
        String os,

        /**
         * 登录时间
         */
        Long loginTime
) {

}
