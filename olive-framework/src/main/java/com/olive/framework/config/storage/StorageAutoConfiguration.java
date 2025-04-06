package com.olive.framework.config.storage;

import com.olive.model.properties.MinioProperties;
import com.olive.model.properties.StorageProperties;
import com.olive.service.storage.FileStorageService;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装配
 *
 * @author jhlz
 * @version x.x.x
 */
@Configuration
public class StorageAutoConfiguration {

    @Bean
    public FileStorageService fileStorageService(StorageProperties properties) {
        return FileStorageServiceFactory.create(properties);
    }

    @Bean
    @ConditionalOnProperty(value = "storage.type", havingValue = "minio")
    public MinioClient client(StorageProperties properties) {
        MinioProperties minio = properties.minio();
        return MinioClient.builder()
                .endpoint(minio.url())
                .credentials(minio.accessKey(), minio.secretKey())
                .build();
    }
}
