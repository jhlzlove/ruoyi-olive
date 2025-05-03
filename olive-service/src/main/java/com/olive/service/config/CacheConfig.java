package com.olive.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olive.model.constant.CacheConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
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
 * 统一缓存配置，包括过期的时间
 * 
 * @author ruoyi
 */
@Configuration
public class CacheConfig {
    private final ObjectMapper objectMapper;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    @Value(value = "${user.ip.maxRetryCount:15}")
    public int maxIpRetryCount;

    @Value(value = "${user.ip.lockTime:15}")
    public int ipLockTime;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    public CacheConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(objectMapper, Object.class);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 默认过期时间为 1 天
                .entryTtl(Duration.ofDays(1))
                .computePrefixWith(name -> name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // 不同缓存的过期时间配置
        Map<String, RedisCacheConfiguration> redisCacheMap = new HashMap<>();
        // 配置缓存一小时
        redisCacheMap.put(CacheConstant.CACHE_CONFIG_KEY, config.entryTtl(Duration.ofHours(1)));
        // ip 错误重试次数，可配置，默认 15 分钟
        redisCacheMap.put(CacheConstant.IP_ERR_CNT_KEY, config.entryTtl(Duration.ofMinutes(ipLockTime)));
        // 密码错误重试，可配置
        redisCacheMap.put(CacheConstant.PWD_ERR_CNT_KEY, config.entryTtl(Duration.ofMinutes(lockTime)));
        // token 过期时间，可配置
        redisCacheMap.put(CacheConstant.CACHE_LOGIN_TOKEN_KEY, config.entryTtl(Duration.ofMinutes(expireTime)));
        // 验证码 2 分钟过期
        redisCacheMap.put(CacheConstant.CAPTCHA_CODE_KEY, config.entryTtl(Duration.ofMinutes(2)));
        return RedisCacheManager
                .builder(connectionFactory)
                .withInitialCacheConfigurations(redisCacheMap)
                .cacheDefaults(config).transactionAware()
                .build();
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(objectMapper, Object.class);
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
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }
}
