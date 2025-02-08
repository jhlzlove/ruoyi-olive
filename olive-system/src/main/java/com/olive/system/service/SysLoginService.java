package com.olive.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.Constants;
import com.olive.framework.constant.UserConstants;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.exception.user.*;
import com.olive.framework.manager.AsyncManager;
import com.olive.framework.manager.factory.AsyncFactory;
import com.olive.framework.util.MessageUtils;
import com.olive.framework.util.StringUtils;
import com.olive.framework.util.ip.IpUtils;
import com.olive.framework.web.system.service.SysPasswordService;
import com.olive.framework.web.system.service.TokenService;
import com.olive.framework.web.system.LoginUser;
import com.olive.framework.web.system.SysUser;
import com.olive.framework.web.system.SysUserDraft;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.ImmutableObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Component
@AllArgsConstructor
public class SysLoginService {
    private static final Logger log = LoggerFactory.getLogger(SysLoginService.class);
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final SysUserService userService;
    private final SysConfigService configService;
    private final SysPasswordService passwordService;
    private final CacheService cacheService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        String ip = IpUtils.getIpAddr();
        // 验证 IP 是否被封锁
        passwordService.validateIp(ip);
        try {
            UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);

            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
            LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
            // 记录用户信息
            recordLoginInfo(loginUser.getUserId());
            // 生成token
            String token = tokenService.createToken(loginUser);

            return token;
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                passwordService.incrementIpFailCount(ip);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(final String username, String code, String uuid) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            String captcha = cacheService.get(CacheConstants.CAPTCHA_CODE_KEY + ":" + uuid);
            cacheService.remove(CacheConstants.CAPTCHA_CODE_KEY + ":" + uuid);
            // String captcha = CacheUtils.get(CacheConstants.CAPTCHA_CODE_KEY, StringUtils.nvl(uuid, ""), String.class);
            // CacheUtils.removeIfPresent(CacheConstants.CAPTCHA_CODE_KEY, StringUtils.nvl(uuid, ""));
            if (captcha == null) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            if (!code.equalsIgnoreCase(captcha)) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     *
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(final String username, String password) {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        try {
            SysUser user = ImmutableObjects.fromString(SysUser.class, sysUser.toString());
            SysUser target = SysUserDraft.$.produce(user, draft -> {
                draft.setLoginIp(IpUtils.getIpAddr())
                        .setLoginDate(ZonedDateTime.now());
            });
            int i = userService.updateUserProfile(target);
            log.info("登录信息记录结果：{}", i);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
