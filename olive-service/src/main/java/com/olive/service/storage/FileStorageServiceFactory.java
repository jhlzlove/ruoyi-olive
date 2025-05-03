package com.olive.service.storage;

/**
 * 文件存储工厂
 *
 * @author jhlz
 * @version x.x.x
 */
public class FileStorageServiceFactory {
    public static FileStorageService create(StorageProperties properties) {
        return switch (properties.type()) {
            case LOCAL -> new LocalStorageService(properties.local());
            case MINIO -> new MinioService(properties.minio());
        };
    }
}
