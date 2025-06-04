package com.olive.service;

import com.olive.model.SysUser;
import com.olive.model.constant.CacheConstant;
import com.olive.model.constant.AppConstant;
import com.olive.model.exception.SecurityException;
import com.olive.service.cache.CacheProperties;
import com.olive.service.manager.AsyncManager;
import com.olive.service.manager.factory.AsyncFactory;
import com.olive.service.util.MessageUtils;
import com.olive.service.security.SecurityUtils;
import com.olive.service.util.ip.IpUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 登录密码方法
 *
 * @author ruoyi
 */
@Component
public class SysPasswordService {

    // @Value(value = "${user.password.maxRetryCount}")
    // private int maxRetryCount;
    //
    // @Value(value = "${user.password.lockTime}")
    // private int lockTime;
    //
    // @Value(value = "${user.ip.maxRetryCount:15}")
    // public int maxIpRetryCount;
    //
    // @Value(value = "${user.ip.lockTime:15}")
    // public int ipLockTime;

    private final CacheManager cacheManager;
    private final CacheProperties cacheProperties;

    public SysPasswordService(CacheManager cacheManager, CacheProperties cacheProperties) {
        this.cacheManager = cacheManager;
        this.cacheProperties = cacheProperties;
    }

    public void validate(SysUser user) {
        Authentication usernamePasswordAuthenticationToken = SecurityContextHolder.getContext().getAuthentication();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        String ip = IpUtils.getIpAddr();
        validateIp(ip);
        Cache cache = cacheManager.getCache(CacheConstant.PWD_ERR_CNT_KEY);
        Integer retryCount = cache.get(username, Integer.class);
        if (retryCount == null) {
            retryCount = 0;
        }
        Integer maxRetryCount = cacheProperties.password().maxRetryCount();
        Integer lockTime = cacheProperties.password().lockTime();
        if (retryCount >= maxRetryCount) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime)));
            throw SecurityException.passwordRetryExceeded("输入密码次数过多，请稍后再试！");
        }
        if (!matches(user, password)) {
            retryCount = retryCount + 1;
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, AppConstant.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.count", retryCount)));
            cache.put(username, retryCount);
            throw SecurityException.usernamePasswordNotMatch("用户名和密码不匹配");
        } else {
            clearLoginRecordCache(username);
        }
    }

    public void validateIp(String ip) {
        Cache cache = cacheManager.getCache(CacheConstant.IP_ERR_CNT_KEY);
        Integer ipRetryCount = cache.get(ip, Integer.class);
        if (ipRetryCount == null) {
            ipRetryCount = 0;
        }
        Integer maxRetryCount = cacheProperties.ip().maxRetryCount();
        if (ipRetryCount >= maxRetryCount) {
            throw SecurityException.ipRetryExceeded("ip 重试次数超过限定，已被锁定");
        }
    }

    public void incrementIpFailCount(String ip) {
        Cache cache = cacheManager.getCache(CacheConstant.IP_ERR_CNT_KEY);
        Integer ipRetryCount = cache.get(ip, Integer.class);
        if (ipRetryCount == null) {
            ipRetryCount = 0;
        }
        ipRetryCount += 1;
        cache.put(ip, ipRetryCount);
    }

    public boolean matches(SysUser user, String rawPassword) {
        return SecurityUtils.matchesPassword(rawPassword, user.password());
    }

    public void clearLoginRecordCache(String loginName) {
        cacheManager.getCache(CacheConstant.PWD_ERR_CNT_KEY).evict(loginName);
    }
}
