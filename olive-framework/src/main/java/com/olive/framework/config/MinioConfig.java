package com.olive.framework.config;

import com.olive.framework.util.StringUtils;
import com.olive.framework.util.file.FileUtils;
import com.olive.framework.config.properties.MinioClientProperties;
import com.olive.framework.minio.MinioBucket;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration("MinioConfiguration")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
@ConfigurationProperties("minio")
public class MinioConfig implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);
    public static int maxSize;
    private String prefix = "/minio";
    private Map<String, MinioClientProperties> client;
    private String primary;
    private Map<String, MinioBucket> targetMinioBucket = new HashMap<>();
    private MinioBucket masterBucket;

    @Override
    public void afterPropertiesSet() throws Exception {
        client.forEach((name, props) -> targetMinioBucket.put(name, createMinioClient(name, props)));
        if (targetMinioBucket.get(primary) == null) {
            throw new RuntimeException("Primary client " + primary + " does not exist");
        }
        masterBucket = targetMinioBucket.get(primary);
    }

    private static void validateMinioBucket(MinioBucket minioBucket) {
        BucketExistsArgs bucketExistArgs = BucketExistsArgs.builder().bucket(minioBucket.getBuketName()).build();
        boolean b = false;
        try {
            b = minioBucket.getClient().bucketExists(bucketExistArgs);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(FileUtils.getRelativePath(AppConfig.getProfile()) + "/")
                    .stream(InputStream.nullInputStream(), 0, -1).bucket(minioBucket.getBuketName()).build();
            minioBucket.getClient().putObject(putObjectArgs);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        if (!b) {
            throw new RuntimeException("Bucket " + minioBucket.getBuketName() + " does not exist");
        }
    }

    private MinioBucket createMinioClient(String name, MinioClientProperties props) {
        MinioClient client;
        if (StringUtils.isEmpty(props.getAccessKey())) {
            client = MinioClient.builder()
                    .endpoint(props.getUrl())
                    .build();
        } else {
            client = MinioClient.builder()
                    .endpoint(props.getUrl())
                    .credentials(props.getAccessKey(), props.getSecretKey())
                    .build();
        }
        MinioBucket minioBucket = new MinioBucket(client, props.getBuketName());
        validateMinioBucket(minioBucket);
        log.info("数据桶：{}  - 链接成功", name);
        return minioBucket;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public MinioBucket getMasterBucket() {
        return this.masterBucket;
    }

    public MinioBucket getBucket(String clent) {
        return targetMinioBucket.get(clent);
    }

    public Map<String, MinioClientProperties> getClient() {
        return client;
    }

    public void setClient(Map<String, MinioClientProperties> client) {
        this.client = client;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
