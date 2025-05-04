package com.olive.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olive.model.constant.CacheConstant;
import com.olive.service.cache.CacheProperties;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一缓存配置，包括过期的时间，可以使用注解，也可以使用编程方式缓存(注入 CacheManager)
 * 
 * @author ruoyi
 */
@EnableCaching
@Configuration
@AllArgsConstructor
public class CacheConfig {
    private final ObjectMapper objectMapper;
    private final CacheProperties cacheProperties;
    private final AppProperties appProperties;

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        var serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 默认过期时间为 1 天
                .entryTtl(Duration.ofDays(1))
                .computePrefixWith(name -> name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        // 不同缓存的过期时间配置
        final Map<String, RedisCacheConfiguration> redisCacheMap = new HashMap<>();
        cacheMapConfig(redisCacheMap, config);

        return RedisCacheManager
                .builder(connectionFactory)
                .withInitialCacheConfigurations(redisCacheMap)
                .cacheDefaults(config).transactionAware()
                .build();
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        var serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        // 设置序列化、反序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        // 确保所有属性都初始化完成
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return """
                local key = KEYS[1]
                local count = tonumber(ARGV[1])
                local time = tonumber(ARGV[2])
                local current = redis.call('get', key);
                if current and tonumber(current) > count then
                    return tonumber(current);
                end
                current = redis.call('incr', key)
                if tonumber(current) == 1 then
                    redis.call('expire', key, time)
                end
                return tonumber(current);""";
    }

    private void cacheMapConfig(Map<String, RedisCacheConfiguration> redisCacheMap, RedisCacheConfiguration config) {
        CacheProperties.CacheLimitProperties password = cacheProperties.password();
        CacheProperties.CacheLimitProperties ip = cacheProperties.ip();
        // 配置缓存一小时
        redisCacheMap.put(CacheConstant.CACHE_CONFIG_KEY, config.entryTtl(Duration.ofHours(1)));
        // 验证码 2 分钟
        redisCacheMap.put(CacheConstant.CAPTCHA_CODE_KEY, config.entryTtl(Duration.ofMinutes(2)));
        // ip 错误重试
        redisCacheMap.put(CacheConstant.IP_ERR_CNT_KEY, config.entryTtl(Duration.ofMinutes(ip.lockTime())));
        // 密码错误重试
        redisCacheMap.put(CacheConstant.PWD_ERR_CNT_KEY, config.entryTtl(Duration.ofMinutes(password.lockTime())));
        // token 过期时间
        redisCacheMap.put(CacheConstant.CACHE_LOGIN_TOKEN_KEY, config.entryTtl(Duration.ofMinutes(appProperties.token().expireTime())));
    }
}
