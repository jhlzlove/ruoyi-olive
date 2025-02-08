package com.olive.framework.config;

import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * https://babyfish-ct.github.io/jimmer-doc/zh/docs/mapping/base/naming-strategy#%E4%BD%BF%E7%94%A8springboot%E6%97%B6
 *
 * @author jhlz
 * @version x.x.x
 */
@Configuration
public class JimmerConfig {

    @Bean
    public DatabaseNamingStrategy databaseNamingStrategy() {
        return DefaultDatabaseNamingStrategy.LOWER_CASE;
    }

    // @Bean
    // public CacheFactory cacheFactory(RedisConnectionFactory connectionFactory,
    //                                  ObjectMapper objectMapper) {
    //     return new CacheFactory() {
    //         @Override
    //         public Cache<?, ?> createObjectCache(ImmutableType type) {
    //             return new ChainCacheBuilder<Object, Object>()
    //                     .add(RedisValueBinder
    //                             .forObject(type)
    //                             .redis(connectionFactory)
    //                             .objectMapper(objectMapper)
    //                             .duration(Duration.ofHours(24))
    //                             .build()
    //                     )
    //                     .build();
    //         }
    //     };
    // }
}
