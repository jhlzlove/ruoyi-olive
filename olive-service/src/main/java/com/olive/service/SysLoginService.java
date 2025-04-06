package com.olive.service;

import com.olive.model.LoginUser;
import com.olive.model.SysUserTable;
import com.olive.model.constant.AppConstant;
import com.olive.model.constant.CacheConstant;
import com.olive.model.exception.SecurityException;
import com.olive.service.manager.AsyncManager;
import com.olive.service.manager.factory.AsyncFactory;
import com.olive.service.util.MessageUtils;
import com.olive.service.util.ip.IpUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.babyfish.jimmer.sql.JSqlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final CacheManager cacheManager;
    private final JSqlClient sqlClient;

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

            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
            LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
            // 记录用户信息
            recordLoginInfo(loginUser.getUserId());
            // 生成token
            return tokenService.createToken(loginUser);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw SecurityException.usernamePasswordNotMatch("用户名密码不匹配！");
            } else {
                passwordService.incrementIpFailCount(ip);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, e.getMessage()));
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(final String username, String code, String uuid) {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        Cache cache = cacheManager.getCache(CacheConstant.CAPTCHA_CODE_KEY);
        if (captchaEnabled) {
            String captcha = cache.get(uuid, String.class);
            cache.evict(uuid);
            if (captcha == null) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw SecurityException.captchaIsNull("验证码为空！");
            }
            if (!code.equalsIgnoreCase(captcha)) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw SecurityException.captchaException("验证码异常！");
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
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UsernameNotFoundException("用户不存在");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < AppConstant.PASSWORD_MIN_LENGTH
                || password.length() > AppConstant.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw SecurityException.usernamePasswordNotMatch("用户名密码不匹配！");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < AppConstant.USERNAME_MIN_LENGTH
                || username.length() > AppConstant.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw SecurityException.usernamePasswordNotMatch("用户名密码不匹配！");
        }
        // IP黑名单校验
        String blackStr = configService.listByConfigKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw SecurityException.inBlackList("已被加入黑名单，请联系管理员！");
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUserTable table = SysUserTable.$;
        sqlClient.createUpdate(table)
                .where(table.userId().eq(userId))
                .set(table.loginIp(), IpUtils.getIpAddr())
                .set(table.loginDate(), ZonedDateTime.now())
                .execute();
    }
}
