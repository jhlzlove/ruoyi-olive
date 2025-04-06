package com.olive.api.monitor;

import com.olive.model.constant.CacheConstant;
import com.olive.model.record.SysCache;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 缓存监控
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/monitor/cache")
public class CacheController {

    private static final Logger log = LoggerFactory.getLogger(CacheController.class);
    private final CacheManager cacheManager;
    private final RedisTemplate redisTemplate;

    private final static List<SysCache> caches = new ArrayList<SysCache>();

    {
        caches.add(new SysCache(CacheConstant.CACHE_LOGIN_TOKEN_KEY, "用户信息"));
        caches.add(new SysCache(CacheConstant.CACHE_CONFIG_KEY, "配置信息"));
        caches.add(new SysCache(CacheConstant.CACHE_DICT_KEY, "数据字典"));
        caches.add(new SysCache(CacheConstant.CAPTCHA_CODE_KEY, "验证码"));
        caches.add(new SysCache(CacheConstant.PHONE_CODES, "短信验证码"));
        caches.add(new SysCache(CacheConstant.EMAIL_CODES, "邮箱验证码"));
        caches.add(new SysCache(CacheConstant.REPEAT_SUBMIT_KEY, "防重提交"));
        caches.add(new SysCache(CacheConstant.RATE_LIMIT_KEY, "限流处理"));
        caches.add(new SysCache(CacheConstant.PWD_ERR_CNT_KEY, "密码错误次数"));
        caches.add(new SysCache(CacheConstant.IP_ERR_CNT_KEY, "IP错误次数"));
        caches.add(new SysCache(CacheConstant.FILE_MD5_PATH_KEY, "path-md5"));
        caches.add(new SysCache(CacheConstant.FILE_PATH_MD5_KEY, "md5-path"));
    }

    /**
     * 缓存监控信息
     *
     * @return map
     */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    public Map<String, Object> getInfo() {
        Properties info = (Properties) redisTemplate
                .execute((RedisCallback<Object>) connection ->
                        connection.commands().info()
                );
        Properties commandStats = (Properties) redisTemplate
                .execute((RedisCallback<Object>) connection ->
                        connection.commands().info("commandstats")
                );
        Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.commands().dbSize()
        );

        List<Map<String, String>> pieList = commandStats
                .stringPropertyNames()
                .stream()
                .map(key -> {
                    String property = commandStats.getProperty(key);
                    return Map.of(
                            "name", StringUtils.removeStart(key, "cmdstat_"),
                            "value", StringUtils.substringBetween(property, "calls=", ",usec")
                    );
                }).toList();
        return Map.of("info", info, "dbSize", dbSize, "commandStats", pieList);
    }

    /**
     * 缓存列表
     *
     * @return 缓存列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getNames")
    public List<SysCache> cache() {
        return caches;
    }

    /**
     * 键名列表
     *
     * @param cacheName 缓存名称
     * @return 缓存键列表
     */
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public List<String> getCacheKeys(@PathVariable String cacheName) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(cacheName + ":*").build();
        return redisTemplate.scan(scanOptions).stream().toList();
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public SysCache getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
        String value = cacheManager.getCache(cacheName).get(cacheKey, String.class);
        SysCache cache = Objects.isNull(value) ?
                new SysCache(cacheName, cacheKey, null) :
                new SysCache(cacheName, cacheKey, value);
        return cache;
    }

    /**
     * 根据名称删除缓存
     * @param cacheName 缓存名称
     * @return  R
     */
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public void clearCacheName(@PathVariable String cacheName) {
        redisTemplate.scan(ScanOptions.scanOptions().match(cacheName + ":*").build())
                .stream()
                .forEach(redisTemplate::delete);
    }


    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public void clearCacheKey(@PathVariable String cacheKey) {
        redisTemplate.scan(ScanOptions.scanOptions().match(cacheKey).build())
                .stream()
                .forEach(redisTemplate::delete);
    }

    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public void clearCacheAll() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}
