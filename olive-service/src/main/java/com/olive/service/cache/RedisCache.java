package com.olive.service.cache;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/// # redis 缓存工具类
/// 基于 RedisTemplate，交给 Spring 工厂托管，使用时直接注入
///
/// > 使用 Spring CacheManager 还是推荐使用注解的方式的，不然真不如直接 CacheUtil
///
/// @author jhlz
/// @version 0.0.1
@Component
public class RedisCache<T> {

    private final RedisTemplate<String, T> redisTemplate;
    // 默认的过期时间
    private final int DEFAULT_EXPIRE_TIME = 1;

    public RedisCache(RedisTemplate<String, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取缓存值
     *
     * @param key key
     * @return value
     */
    public T get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 字符串缓存，默认过期时间 1 day，避免僵尸内存，若不满足需求使用重载方法自定义
     *
     * @param key   key
     * @param value value
     * @see RedisCache#put(String, Object, long, TimeUnit)
     */
    public void put(String key, T value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.DAYS);
    }

    /**
     * 字符串缓存
     *
     * @param key        key，不能为 null
     * @param value      value，不能为 null
     * @param expireTime 过期时间
     * @param unit       过期时间单位，不能为 null
     */
    public void put(String key, T value, long expireTime, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, expireTime, unit);
    }

    /**
     * 删除缓存值
     *
     * @param key key，不能为 null
     */
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存值
     *
     * @param key key 前缀，不能为 null
     */
    public void clean(String key) {
        ScanOptions options = ScanOptions.scanOptions().match(key).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursors = redisTemplate.scan(options)) {
            while (cursors.hasNext()) {
                String next = cursors.next();
                keys.add(next);
            }
        }
        redisTemplate.delete(keys);
    }
}
