package com.olive.service.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(StorageAutoConfiguration.class);

    @Bean
    public FileStorageService fileStorageService(StorageProperties properties) {
        return FileStorageServiceFactory.create(properties);
    }

    /**
     * 初始化 minio，同时创建默认桶
     *
     * @param properties minio 配置
     * @return minioClient
     */
    @Bean
    @SneakyThrows
    @ConditionalOnProperty(value = "storage.type", havingValue = "minio")
    public MinioClient client(StorageProperties properties) {
        StorageProperties.MinioProperties minio = properties.minio();
        MinioClient client = MinioClient.builder()
                .endpoint(minio.url())
                .credentials(minio.accessKey(), minio.secretKey())
                .build();
        BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(minio.defaultBucket()).build();
        if (!client.bucketExists(existsArgs)) {
            log.info("default bucket not exists, creating...");
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(minio.defaultBucket()).build();
            client.makeBucket(makeBucketArgs);
            log.info("success creating!");
        }
        log.info("minio server connected");
        return client;
    }
}
