package com.olive.framework.util.file;

import com.olive.framework.cache.CacheService;
import com.olive.framework.constant.CacheConstants;
import com.olive.framework.file.FileService;
import com.olive.framework.util.Md5Utils;
import com.olive.framework.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件上传工具类
 *
 * @author ruoyi
 */
public class FileOperateUtils {

    private static FileService fileService = SpringUtils.getBean("file:strategy:disk");
    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

    /**
     * 默认上传的地址
     */

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件路径
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException {
        try {
            String md5 = Md5Utils.getMd5(file);
            String pathForMd5 = FileOperateUtils.getFilePathForMd5(md5);
            if (StringUtils.isNotEmpty(pathForMd5)) {
                return pathForMd5;
            }
            FileUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
            String pathFileName = fileService.upload(file);
            FileOperateUtils.saveFileAndMd5(pathFileName, md5);
            return pathFileName;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param filePath         上传文件的路径
     * @param file             上传的文件
     * @param allowedExtension 允许的扩展名
     * @return 文件名称
     * @throws IOException
     */
    public static String upload(String filePath, MultipartFile file, String[] allowedExtension) throws Exception {
        String md5 = Md5Utils.getMd5(file);
        String pathForMd5 = FileOperateUtils.getFilePathForMd5(md5);
        if (StringUtils.isNotEmpty(pathForMd5)) {
            return pathForMd5;
        }
        FileUtils.assertAllowed(file, allowedExtension);
        fileService.upload(filePath, file);
        FileOperateUtils.saveFileAndMd5(filePath, md5);
        return filePath;
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param fileName         上传文件名
     * @param allowedExtension 允许的扩展名
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, String fileName, MultipartFile file,
                                      String[] allowedExtension)
            throws IOException {
        try {
            String filePath = baseDir + File.separator + fileName;
            return upload(filePath, file, allowedExtension);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径下载
     *
     * @param fileUrl      下载文件路径
     * @param outputStream 需要输出到的输出流
     * @return 文件名称
     * @throws IOException
     */
    public static final void downLoad(String fileUrl, OutputStream outputStream) throws Exception {
        InputStream inputStream = fileService.downLoad(fileUrl);
        FileUtils.writeBytes(inputStream, outputStream);
    }

    /**
     * 根据文件路径删除
     *
     * @param fileUrl 下载文件路径
     * @return 是否成功
     * @throws IOException
     */
    public static final boolean deleteFile(String fileUrl) throws Exception {
        return fileService.deleteFile(fileUrl);
    }

    /**
     * 根据md5获取文件的路径
     *
     * @param md5 文件的md5
     * @return
     */
    public static String getFilePathForMd5(String md5) {
        CacheService cacheService = SpringUtils.getBean(CacheService.class);
        return cacheService.get(CacheConstants.FILE_MD5_PATH_KEY + ":" + md5, String.class);
        // return CacheUtils.get(CacheConstants.FILE_MD5_PATH_KEY, md5, String.class);
    }

    /**
     * 保存文件的md5
     *
     * @param path 文件的路径
     * @param md5  文件的md5
     */
    public static void saveFileAndMd5(String path, String md5) {
        CacheService cacheService = SpringUtils.getBean(CacheService.class);
        cacheService.put(CacheConstants.FILE_MD5_PATH_KEY + ":" + md5, path);
        cacheService.put(CacheConstants.FILE_PATH_MD5_KEY + ":" + md5, md5);
        // CacheUtils.put(CacheConstants.FILE_MD5_PATH_KEY, md5, path);
        // CacheUtils.put(CacheConstants.FILE_PATH_MD5_KEY, path, md5);
    }

    /**
     * 删除文件的md5
     *
     * @param md5 文件的md5
     */
    public static void deleteFileAndMd5ByMd5(String md5) {
        String filePathByMd5 = getFilePathForMd5(md5);
        if (StringUtils.isNotEmpty(filePathByMd5)) {
            CacheService cacheService = SpringUtils.getBean(CacheService.class);
            cacheService.remove(CacheConstants.FILE_MD5_PATH_KEY + ":" + md5);
            cacheService.remove(CacheConstants.FILE_PATH_MD5_KEY + ":" + filePathByMd5);
            // CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5);
            // CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePathByMd5);
        }
    }

    public static void deleteFileAndMd5ByFilePath(String filePath) {
        CacheService cacheService = SpringUtils.getBean(CacheService.class);
        String md5ByFilePath = cacheService.get(CacheConstants.FILE_PATH_MD5_KEY + ":" + filePath);
        // String md5ByFilePath = CacheUtils.get(CacheConstants.FILE_PATH_MD5_KEY, filePath, String.class);
        if (StringUtils.isNotEmpty(md5ByFilePath)) {
            cacheService.remove(CacheConstants.FILE_PATH_MD5_KEY + ":" + filePath);
            cacheService.remove(CacheConstants.FILE_MD5_PATH_KEY + ":" + md5ByFilePath);
            // CacheUtils.remove(CacheConstants.FILE_PATH_MD5_KEY, filePath);
            // CacheUtils.remove(CacheConstants.FILE_MD5_PATH_KEY, md5ByFilePath);
        }
    }
}
