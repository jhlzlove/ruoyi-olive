package com.olive.service.cache;

import org.babyfish.jimmer.client.generator.openapi.OpenApiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存相关配置
 * @author jhlz
 * @version 0.0.1
 */
@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
        CacheLimitProperties password,
        CacheLimitProperties ip
)
{

    public record CacheLimitProperties(
            /* 最大重试次数 */
            Integer maxRetryCount,
            /* 锁定时间 */
            Integer lockTime
    ){}
}
