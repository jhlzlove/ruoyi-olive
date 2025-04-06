package com.olive.model.constant;


/**
 * 全局应用常量信息
 *
 * @author ruoyi
 */
public interface AppConstant {

    String Y = "y";

    /**
     * www主域
     */
    String WWW = "www.";

    /**
     * http请求
     */
    String HTTP = "http://";

    /**
     * https请求
     */
    String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    String FAIL = "1";

    /**
     * 登录成功
     */
    String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    String LOGOUT = "Logout";

    /**
     * 注册
     */
    String REGISTER = "Register";

    /**
     * 登录失败
     */
    String LOGIN_FAIL = "Error";

    /**
     * 所有权限标识
     */
    String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    String SUPER_ADMIN = "admin";

    /**
     * 令牌
     */
    String TOKEN = "token";

    /**
     * 令牌前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    String LOGIN_USER_KEY = "login_user_key";

    /**
     * 资源映射路径 前缀
     */
    String RESOURCE_PREFIX = "/profile";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    String[] JSON_WHITELIST_STR = {"org.springframework", "com.olive"};

    /**
     * 定时任务违规的字符
     */
    String[] JOB_ERROR_STR = {
            "java.net.URL", "javax.naming.InitialContext",
            "org.yaml.snakeyaml", "org.springframework", "org.apache",
            "com.olive.common.utils.file", "com.olive.common.config", "com.olive.generator"};

    /**
     * 部门停用状态
     */
    String DEPT_DISABLE = "1";

    /**
     * 是否菜单外链（是）
     */
    String YES_FRAME = "0";

    /**
     * 是否菜单外链（否）
     */
    String NO_FRAME = "1";

    /**
     * 菜单类型（目录）
     */
    String TYPE_DIR = "M";

    /**
     * 菜单类型（菜单）
     */
    String TYPE_MENU = "C";

    /**
     * 菜单类型（按钮）
     */
    String TYPE_BUTTON = "F";

    /**
     * Layout组件标识
     */
    String LAYOUT = "Layout";

    /**
     * ParentView组件标识
     */
    String PARENT_VIEW = "ParentView";

    /**
     * InnerLink组件标识
     */
    String INNER_LINK = "InnerLink";

    /**
     * 用户名长度限制
     */
    int USERNAME_MIN_LENGTH = 2;
    int USERNAME_MAX_LENGTH = 20;

    /**
     * 密码长度限制
     */
    int PASSWORD_MIN_LENGTH = 5;
    int PASSWORD_MAX_LENGTH = 20;
}
