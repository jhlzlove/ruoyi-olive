package com.olive.framework.cache;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jhlz
 * @version x.x.x
 */
@Component
@AllArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedisTemplate redisTemplate;

    @Override
    public Collection<String> keys(String prefix) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(prefix).build();
        Cursor<byte[]> cursor = redisTemplate.scan(scanOptions);
        ArrayList<String> list = new ArrayList<>();
        while (cursor.hasNext()) {
            byte[] next = cursor.next();
            list.add(new String(next));
        }
        return list;
    }

    @Override
    public String get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .orElseGet(String::new)
                .toString();
    }

    @Override
    public String get(String prefix, String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(prefix + ":" + key))
                .orElseGet(String::new)
                .toString();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public void put(String prefix, String key, Object value) {
        redisTemplate.opsForValue().set(prefix + ":" + key, value, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    @Override
    public void put(String key, Object value, long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expire, timeUnit);
    }

    @Override
    public boolean remove(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public boolean remove(String prefix, String key) {
        return redisTemplate.delete(prefix + ":" + key);
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit);
    }

    @Override
    public boolean clear(String prefix) {
        ScanOptions options = ScanOptions.scanOptions().match(prefix + ":*").build();
        redisTemplate.scan(options)
                .stream()
                .forEach(redisTemplate::delete);
        return true;
    }
}
