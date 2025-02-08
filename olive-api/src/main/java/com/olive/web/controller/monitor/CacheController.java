package com.olive.web.controller.monitor;

import com.olive.common.response.R;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.util.CacheUtils;
import com.olive.framework.util.Convert;
import com.olive.framework.util.StringUtils;
import com.olive.system.domain.SysCache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 缓存监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {
    private static String tmpCacheName = "";

    private final static List<SysCache> caches = new ArrayList<SysCache>();

    {
        caches.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, "用户信息"));
        caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
        caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
        caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        caches.add(new SysCache(CacheConstants.PHONE_CODES, "短信验证码"));
        caches.add(new SysCache(CacheConstants.EMAIL_CODES, "邮箱验证码"));
        caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
        caches.add(new SysCache(CacheConstants.IP_ERR_CNT_KEY, "IP错误次数"));
        caches.add(new SysCache(CacheConstants.FILE_MD5_PATH_KEY, "path-md5"));
        caches.add(new SysCache(CacheConstants.FILE_PATH_MD5_KEY, "md5-path"));
    }


    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getNames")
    public R cache() {
        return R.ok(caches);
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public R getCacheKeys(@PathVariable String cacheName) {
        tmpCacheName = cacheName;
        Set<String> keyset = CacheUtils.getkeys(cacheName);
        return R.ok(keyset);
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public R getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        ValueWrapper valueWrapper = CacheUtils.get(cacheName, cacheKey);
        SysCache sysCache = new SysCache();
        sysCache.setCacheName(cacheName);
        sysCache.setCacheKey(cacheKey);
        if (StringUtils.isNotNull(valueWrapper)) {
            sysCache.setCacheValue(Convert.toStr(valueWrapper.get(), ""));
        }
        return R.ok(sysCache);
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public R clearCacheName(@PathVariable String cacheName) {
        CacheUtils.clear(cacheName);
        return R.ok();
    }


    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public R clearCacheKey(@PathVariable String cacheKey) {
        CacheUtils.removeIfPresent(tmpCacheName, cacheKey);
        return R.ok();
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public R clearCacheAll() {
        for (String cacheName : CacheUtils.getCacheManager().getCacheNames()) {
            CacheUtils.clear(cacheName);
        }
        return R.ok();
    }
}
