package com.olive.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jhlz
 * @version 0.0.1
 */
@ConfigurationProperties("app")
public record AppProperties(
        String captchaType,
        TokenProperties token
) {
    public record TokenProperties(
            String header,
            String secret,
            int expireTime
    ){}
}
