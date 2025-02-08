package com.olive.framework.web.system.service;

import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.constant.Constants;
import com.olive.framework.exception.user.IpRetryLimitExceedException;
import com.olive.framework.exception.user.UserPasswordNotMatchException;
import com.olive.framework.exception.user.UserPasswordRetryLimitExceedException;
import com.olive.framework.manager.AsyncManager;
import com.olive.framework.manager.factory.AsyncFactory;
import com.olive.framework.util.MessageUtils;
import com.olive.framework.util.SecurityUtils;
import com.olive.framework.util.ip.IpUtils;
import com.olive.framework.web.system.SysUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录密码方法
 *
 * @author ruoyi
 */
@Component
public class SysPasswordService {

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    @Value(value = "${user.ip.maxRetryCount:15}")
    public int maxIpRetryCount;

    @Value(value = "${user.ip.lockTime:15}")
    public int ipLockTime;

    private final CacheService cacheService;

    public SysPasswordService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void validate(SysUser user) {
        Authentication usernamePasswordAuthenticationToken = SecurityContextHolder.getContext().getAuthentication();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        String ip = IpUtils.getIpAddr();
        validateIp(ip);
        Integer retryCount = cacheService.get(CacheConstants.PWD_ERR_CNT_KEY + ":" +  username, Integer.class);
        // Integer retryCount = getCache().get(username, Integer.class);
        if (retryCount == null) {
            retryCount = 0;
        }
        if (retryCount >= Integer.valueOf(maxRetryCount).intValue()) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime)));
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }
        if (!matches(user, password)) {
            retryCount = retryCount + 1;
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.count", retryCount)));
            // CacheUtils.put(CacheConstants.PWD_ERR_CNT_KEY, username, retryCount, lockTime, TimeUnit.MINUTES);
            cacheService.put(CacheConstants.PWD_ERR_CNT_KEY + ":" + username, retryCount, lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        } else {
            clearLoginRecordCache(username);
        }
    }

    public void validateIp(String ip) {
        // Integer ipRetryCount = getIpCache().get(ip, Integer.class);
        Integer ipRetryCount = cacheService.get(CacheConstants.IP_ERR_CNT_KEY + ":" + ip, Integer.class);
        if (ipRetryCount == null) {
            ipRetryCount = 0;
        }

        if (ipRetryCount >= maxIpRetryCount) {
            throw new IpRetryLimitExceedException(maxIpRetryCount, ipLockTime);
        }
    }

    public void incrementIpFailCount(String ip) {
        // Integer ipRetryCount = getIpCache().get(ip, Integer.class);
        Integer ipRetryCount = cacheService.get(CacheConstants.IP_ERR_CNT_KEY + ":" + ip, Integer.class);
        if (ipRetryCount == null) {
            ipRetryCount = 0;
        }
        ipRetryCount += 1;
        cacheService.put(CacheConstants.IP_ERR_CNT_KEY + ":" + ip, ipRetryCount, lockTime, TimeUnit.MINUTES);
        // CacheUtils.put(CacheConstants.IP_ERR_CNT_KEY, ip, ipRetryCount, ipLockTime, TimeUnit.MINUTES);
    }

    public boolean matches(SysUser user, String rawPassword) {
        return SecurityUtils.matchesPassword(rawPassword, user.password());
    }

    public void clearLoginRecordCache(String loginName) {
        cacheService.remove(CacheConstants.PWD_ERR_CNT_KEY + ":" + loginName);
    }
}
