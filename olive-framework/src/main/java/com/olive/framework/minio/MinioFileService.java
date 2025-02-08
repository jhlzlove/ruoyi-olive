package com.olive.framework.minio;


import com.olive.framework.config.AppConfig;
import com.olive.framework.file.FileService;
import com.olive.framework.util.file.FileOperateUtils;
import com.olive.framework.util.file.FileUtils;
import com.olive.framework.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 * Minio文件操作实现类
 */
@Component("file:strategy:minio")
@ConditionalOnProperty(prefix = "minio", name = { "enable" }, havingValue = "true", matchIfMissing = false)
public class MinioFileService implements FileService {
    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        String relativePath = null;
        if (FileUtils.isAbsolutePath(filePath)) {
            relativePath = FileUtils.getRelativePath(filePath);
        } else {
            relativePath = filePath;
        }

        return MinioUtil.uploadFile(minioConfig.getPrimary(), relativePath, file);
    }

    @Override
    public String upload(MultipartFile file, String name) throws Exception {
        return MinioUtil.uploadFile(minioConfig.getPrimary(), name, file);
    }

    @Override
    public String upload(MultipartFile file) throws Exception {
        String filePath = AppConfig.getProfile() + File.separator + FileUtils.fastFilePath(file);
        return upload(filePath, file);
    }

    @Override
    public String upload(String baseDir, String fileName, MultipartFile file) throws Exception {
        return upload(baseDir + File.pathSeparator + fileName, file);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        // String filePath = StringUtils.substringAfter(filePath, "?fileName=");
        MinioFileVO file = MinioUtil.getFile(minioConfig.getPrimary(), filePath);
        return file.getFileInputSteam();
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        MinioUtil.removeFile(minioConfig.getPrimary(), filePath);
        FileOperateUtils.deleteFileAndMd5ByFilePath(filePath);
        return true;
    }
}
