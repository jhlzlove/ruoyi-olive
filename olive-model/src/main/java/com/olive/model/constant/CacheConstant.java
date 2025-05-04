package com.olive.model.constant;

/// # 全局缓存常量
/// 缓存名称全部使用 **小写单数**
///
/// @author jhlz
/// @version 0.0.1
public interface CacheConstant {
    /**
     * sys_config key
     */
    String CACHE_CONFIG_KEY = "sys_config";

    /**
     * 验证码是否开启 key
     */
    String CACHE_CAPTCHA_KEY = "sys.account.captchaEnabled";

    String CACHE_DICT_KEY = "sys_dict";

    /**
     * 登录用户 redis key
     */
    String CACHE_LOGIN_TOKEN_KEY = "login_token";

    /**
     * 验证码 redis key
     */
    String CAPTCHA_CODE_KEY = "captcha_code";

    /**
     * 登录账户密码错误次数 redis key
     */
    String PWD_ERR_CNT_KEY = "pwd_err_cnt";

    /**
     * 登录ip错误次数 redis key
     */
    String IP_ERR_CNT_KEY = "ip_err_cnt_key";

    /**
     * 防重提交 redis key
     */
    String REPEAT_SUBMIT_KEY = "repeat_submit";

    /**
     * 限流 redis key
     */
    String RATE_LIMIT_KEY = "rate_limit";

    /**
     * 手机号验证码 phone codes
     */
    String PHONE_CODES = "phone_code";

    /**
     * 邮箱验证码
     */
    String EMAIL_CODES = "email_code";

    /**
     * 文件的md5 redis key
     */
    String FILE_MD5_PATH_KEY = "file_md5_path";

    /**
     * 文件路径 redis key
     */
    String FILE_PATH_MD5_KEY = "file_path_md5";
}
