package com.olive.model.properties;

import com.olive.model.constant.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置读取
 *
 * @author jhlz
 * @version x.x.x
 */
@ConfigurationProperties("storage")
public record StorageProperties(
         StorageType type,
         MinioProperties minio,
         LocalProperties local
) {
}

