package com.olive.service.storage;

import com.olive.model.constant.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置读取
 *
 * @author jhlz
 * @version x.x.x
 */
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        StorageType type,
        MinioProperties minio,
        LocalProperties local
) {

    public record MinioProperties(
            String url,
            String accessKey,
            String secretKey,
            String defaultBucket
    ) {
    }

    public record LocalProperties(
            String basePath
    ) {
    }
}
