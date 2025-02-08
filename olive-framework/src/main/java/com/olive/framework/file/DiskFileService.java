package com.olive.framework.file;

import com.olive.common.utils.LocalDateUtil;
import com.olive.common.utils.uuid.UUID;
import com.olive.framework.config.AppConfig;
import com.olive.framework.constant.Constants;
import com.olive.framework.exception.file.FileNameLengthLimitExceededException;
import com.olive.framework.util.Md5Utils;
import com.olive.framework.util.file.FileOperateUtils;
import com.olive.framework.util.file.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;

import static com.olive.framework.util.file.FileUtils.getAbsoluteFile;
import static com.olive.framework.util.file.FileUtils.getPathFileName;


/**
 * 磁盘文件操作实现类
 */
@Component("file:strategy:disk")
public class DiskFileService implements FileService {

    private static String defaultBaseDir = AppConfig.getProfile();

    public static void setDefaultBaseDir(String defaultBaseDir) {
        DiskFileService.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    @Override
    public String upload(String filePath, MultipartFile file) throws Exception {
        int fileNamelength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNamelength > FileUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        // String fileName = extractFilename(file);

        String absPath = getAbsoluteFile(filePath).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getPathFileName(filePath);
    }

    @Override
    public String upload(MultipartFile file, String name) throws Exception {
        try {
            return upload(getDefaultBaseDir(), file);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String upload(MultipartFile file) throws Exception {
        try {
            String date = LocalDateUtil.dateToStr(LocalDateUtil.date(), "yyyyMMdd");
            String dateTime = LocalDateUtil.dateTimeToStr(LocalDateUtil.dateTime(), "yyyyMMddHHmmss");
            String filePath = getDefaultBaseDir() + File.separator + date + File.separator
                    + dateTime + UUID.fastUUID().toString().substring(0, 6)
                    + "." + FileUtils.getExtension(file);
            return upload(filePath, file);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String upload(String baseDir, String fileName, MultipartFile file) throws Exception {
        String filePath = AppConfig.getProfile() + File.separator + baseDir + File.separator + fileName;
        return upload(filePath, file);
    }

    @Override
    public InputStream downLoad(String filePath) throws Exception {
        // 本地资源路径
        String localPath = AppConfig.getProfile();
        // 数据库资源地址
        String downloadPath = localPath + StringUtils.substringAfter(filePath, Constants.RESOURCE_PREFIX);
        // 下载名称

        File file = new File(downloadPath);
        if (!file.exists()) {
            throw new FileNotFoundException("未找到文件");
        }
        return new FileInputStream(file);
    }

    @Override
    public boolean deleteFile(String filePath) throws Exception {
        String relivatePath = StringUtils.substringAfter(filePath, Constants.RESOURCE_PREFIX);
        String fileAbs = AppConfig.getProfile() + relivatePath;
        boolean flag = false;
        File file = new File(fileAbs);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            String md5 = Md5Utils.getMd5(file);
            flag = file.delete();
            if(flag) {
                FileOperateUtils.deleteFileAndMd5ByMd5(md5);
            }
        }
        return flag;
    }

}
