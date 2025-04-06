package com.olive.model.exception;

import org.babyfish.jimmer.error.ErrorFamily;

/**
 * 用户验证信息相关异常
 *
 * @author jhlz
 * @version 0.0.1
 */
@ErrorFamily
public enum SecurityError {

    /**
     * 旧密码错误
     */
    OLD_PASSWORD_ERROR,

    /**
     * 新旧密码相同
     */
    SAME_OLD_PASSWORD,

    /**
     * 密码重试过多
     */
    PASSWORD_RETRY_EXCEEDED,

    /**
     * 同一 ip 重试超过次数
     */
    IP_RETRY_EXCEEDED,

    /**
     * 用户名密码不匹配
     */
    USERNAME_PASSWORD_NOT_MATCH,

    /**
     * 黑名单
     */
    IN_BLACK_LIST,

    /**
     * 验证为空
     */
    CAPTCHA_IS_NULL,

    /**
     * 不允许操作超管
     */
    NOT_ALLOW_OPERATE_SUPER_ADMIN,

    /**
     * 不允许访问用户数据
     */
    NOT_ALLOW_ACCESS_USER_DATA,

    /**
     * 获取用户 id 异常
     */
    GET_USER_ID_EXCEPTION,

    /**
     * 获取部门 id 异常
     */
    GET_DEPT_ID_EXCEPTION,

    /**
     * 获取用户名称异常
     */
    GET_USER_NAME_EXCEPTION,

    /**
     * 获取用户信息
     */
    GET_USER_PRINCIPAL_EXCEPTION,

    /**
     * 验证码异常
     */
    CAPTCHA_EXCEPTION,

    /**
     * 验证码无效
     */
    CAPTCHA_INVALID,

    /**
     * 账户已禁用
     */
    ACCOUNT_DISABLED,

    /**
     * 账户已删除
     */
    ACCOUNT_DELETED,

    ;
}
