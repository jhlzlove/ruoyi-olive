package com.olive.model.properties;

/**
 * @author jhlz
 * @version x.x.x
 */
public record MinioProperties(
        String url,
        String accessKey,
        String secretKey,
        String defaultBucket
) {
}
