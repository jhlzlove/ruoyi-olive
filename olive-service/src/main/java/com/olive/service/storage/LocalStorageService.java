package com.olive.service.storage;

import com.olive.base.utils.LocalDateUtil;
import com.olive.service.util.ServletUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;


/**
 * 磁盘文件操作实现类
 */
@AllArgsConstructor
public class LocalStorageService implements FileStorageService {

    private final StorageProperties.LocalProperties properties;

    @Override
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String newFilename = "";
        if (Objects.nonNull(originalFilename) && originalFilename.indexOf(".") > 0) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            newFilename = LocalDateUtil.toStr(LocalDateUtil.dateTime(), LocalDateUtil.YYYYMMDDHHMMSS) + extension;
        } else {
            newFilename = LocalDateUtil.toStr(LocalDateUtil.dateTime(), LocalDateUtil.YYYYMMDDHHMMSS);
        }

        Path path = Paths.get(properties.basePath() + newFilename);
        try {
            Files.createFile(path);
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ServletUtils.getUrl() + "/" + properties.basePath() + newFilename;
    }

    @Override
    public String upload(@Nullable String bucketName, MultipartFile file) {
        return "";
    }
}
