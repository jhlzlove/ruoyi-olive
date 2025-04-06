package com.olive.framework.config.storage;

import com.olive.model.properties.StorageProperties;
import com.olive.service.storage.DiskFileService;
import com.olive.service.storage.FileStorageService;
import com.olive.service.storage.MinioService;

/**
 * 文件存储工厂
 *
 * @author jhlz
 * @version x.x.x
 */
public class FileStorageServiceFactory {
    public static FileStorageService create(StorageProperties properties) {
        return switch (properties.type()) {
            case LOCAL -> new DiskFileService();
            case MINIO -> new MinioService();
        };
    }
}
