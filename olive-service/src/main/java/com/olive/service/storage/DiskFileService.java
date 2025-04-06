package com.olive.service.storage;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;


/**
 * 磁盘文件操作实现类
 */
public class DiskFileService implements FileStorageService {

    @Override
    public String upload(MultipartFile file) {
        return "local disk server is running!";
    }

    @Override
    public String upload(@Nullable String bucketName, MultipartFile file) {
        return "";
    }
}
