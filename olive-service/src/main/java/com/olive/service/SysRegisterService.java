package com.olive.service;

import com.olive.model.SysUser;
import com.olive.model.SysUserDraft;
import com.olive.model.constant.AppConstant;
import com.olive.model.constant.CacheConstant;
import com.olive.model.exception.SecurityException;
import com.olive.model.record.RegisterBody;
import com.olive.service.manager.AsyncManager;
import com.olive.service.manager.factory.AsyncFactory;
import com.olive.service.util.MessageUtils;
import com.olive.service.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 *
 * @author ruoyi
 */
@Component
@AllArgsConstructor
public class SysRegisterService {
    private final SysUserService userService;
    private final SysConfigService configService;
    private final CacheManager cacheManager;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.username(), password = registerBody.password();

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(registerBody.code(), registerBody.uuid());
        }
        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (username.length() < AppConstant.USERNAME_MIN_LENGTH
                || username.length() > AppConstant.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < AppConstant.PASSWORD_MIN_LENGTH
                || password.length() > AppConstant.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (userService.checkUserNameUnique(username)) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            SysUser user = SysUserDraft.$.produce(draft ->
                    draft.setUserName(username)
                            .setNickName(username)
                            .setPassword(SecurityUtils.encryptPassword(password))
            );
            boolean b = userService.registerUser(user);
            if (!b) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param code 验证码
     * @param uuid 唯一标识
     */
    public void validateCaptcha(String code, String uuid) {
        Cache cache = cacheManager.getCache(CacheConstant.CAPTCHA_CODE_KEY);
        String captcha = cache.get(uuid, String.class);
        cache.evict(uuid);
        if (captcha == null) {
            throw SecurityException.captchaException("验证码异常");
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw SecurityException.captchaInvalid("验证码无效！");
        }
    }
}
