package com.olive.model.constant;

/**
 * 限流类型
 *
 * @author ruoyi
 */

public enum LimitType {
    /**
     * 默认策略全局限流
     */
    DEFAULT,

    /**
     * 根据请求者IP进行限流
     */
    IP,

    /**
     * 根据请求者的用户ID进行限流
     */
    USER,

    /**
     * 根据请求者的部门进行限流
     */
    DEPT,
}
