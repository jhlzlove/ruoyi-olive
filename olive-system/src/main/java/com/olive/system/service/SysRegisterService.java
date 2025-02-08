package com.olive.system.service;

import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.Constants;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.user.CaptchaException;
import com.olive.framework.exception.user.CaptchaExpireException;
import com.olive.framework.manager.AsyncManager;
import com.olive.framework.manager.factory.AsyncFactory;
import com.olive.framework.util.MessageUtils;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.record.RegisterBody;
import com.olive.framework.web.system.SysUser;
import com.olive.framework.web.system.SysUserDraft;
import lombok.AllArgsConstructor;
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
    private final CacheService cacheService;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.username(), password = registerBody.password();

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(username, registerBody.code(), registerBody.uuid());
        }
        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else if (userService.checkUserNameUnique(username)) {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        } else {
            SysUser user = SysUserDraft.$.produce(draft -> {
                draft.setUserName(username)
                        .setNickName(username)
                        .setPassword(SecurityUtils.encryptPassword(password));
            });
            boolean b = userService.registerUser(user);
            if (!b) {
                msg = "注册失败,请联系系统管理人员";
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String captcha = cacheService.get(CacheConstants.CAPTCHA_CODE_KEY + ":" + uuid);
        cacheService.remove(CacheConstants.CAPTCHA_CODE_KEY + ":" + uuid);

        // String captcha = CacheUtils.get(CacheConstants.CAPTCHA_CODE_KEY, StringUtils.nvl(uuid, ""), String.class);
        // CacheUtils.removeIfPresent(CacheConstants.CAPTCHA_CODE_KEY, StringUtils.nvl(uuid, ""));
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
