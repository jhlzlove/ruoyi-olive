package com.olive.framework.cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口
 *
 * @author jhlz
 * @version 0.0.1
 */
public interface CacheService {
    // 默认过期时间 7 天
    long DEFAULT_EXPIRE = 60 * 60 * 24 * 7;

    /**
     * 根据 prefix 获取缓存列表
     *
     * @param prefix prefix
     * @return 字符串
     */
    Collection<String> keys(String prefix);

    /**
     * 根据 key 获取缓存对象
     *
     * @param key key
     * @return 字符串
     */
    String get(String key);

    /**
     * 根据 key 获取缓存对象
     *
     * @param prefix key 前缀
     * @param key    key
     * @return 字符串
     */
    String get(String prefix, String key);

    /**
     * 根据 key 获取缓存对象
     *
     * @param key key
     * @return T
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 缓存对象
     *
     * @param key   key
     * @param value value
     */
    void put(String key, Object value);

    /**
     * 缓存对象
     *
     * @param prefix key 前缀
     * @param key    key
     * @param value  value
     */
    void put(String prefix, String key, Object value);

    /**
     * 缓存对象并设置过期时间
     *
     * @param key      key
     * @param value    value
     * @param expire   缓存时间
     * @param timeUnit 时间单位
     */
    void put(String key, Object value, long expire, TimeUnit timeUnit);

    /**
     * 移除缓存
     *
     * @param key key
     * @return true？
     */
    boolean remove(String key);

    /**
     * 移除缓存
     *
     * @param prefix key 前缀
     * @param key    key
     * @return true？
     */
    boolean remove(String prefix, String key);

    /**
     * 移除缓存
     *
     * @param key      key
     * @param expire   缓存时间
     * @param timeUnit 时间单位
     */
    boolean expire(String key, long expire, TimeUnit timeUnit);

    /**
     * 清除指定 key 前缀的所有缓存
     *
     * @param prefix key 前缀
     * @return true?
     */
    boolean clear(String prefix);
}
