package com.olive.service.storage;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口
 *
 * @author jhlz
 * @version x.x.x
 */
public interface FileStorageService {

    /**
     * 文件上传
     *
     * @param file 上传的文件
     * @return 路径
     */
    String upload(MultipartFile file);

    /**
     * 文件上传
     *
     * @param bucketName 桶名
     * @param file   文件名
     * @return 路径
     */
    String upload(@Nullable String bucketName, MultipartFile file);
}
