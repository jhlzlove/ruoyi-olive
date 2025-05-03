package com.olive.api.common;

import com.olive.service.annotation.Anonymous;
import com.olive.service.config.AppConfig;
import com.olive.service.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@RestController
@AllArgsConstructor
@RequestMapping("/common")
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    private final FileStorageService fileStorageService;
    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("/download")
    @Anonymous
    public void fileDownload(
            @RequestParam("fileName") String fileName,
            @RequestParam("delete") Boolean delete,
            HttpServletResponse response,
            HttpServletRequest request) {
        try {

        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @Anonymous
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestPart("file") MultipartFile file) throws Exception {
        String url = fileStorageService.upload(file);
        return Map.of(
                "url", url,
                // "fileName", fileName,
                // "newFileName", FilenameUtils.getName(fileName),
                "originalFilename", file.getOriginalFilename()
        );
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    @Anonymous
    public Map<String, Object> uploadFiles(@RequestPart List<MultipartFile> files)
            throws Exception {
        try {
            // 上传文件路径
            String filePath = AppConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files) {
                // 上传并返回新文件名称
                String fileName = fileStorageService.upload(file);
                String url = getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FilenameUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            return Map.of(
                    "urls", StringUtils.join(urls, FILE_DELIMETER),
                    "fileNames", StringUtils.join(fileNames, FILE_DELIMETER),
                    "newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER),
                    "originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER)
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    @Anonymous
    public void resourceDownload(@RequestParam String resource,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径
     *
     * @return 服务地址
     */
    private String getUrl() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        StringBuffer url = request.getRequestURL();
        String contextPath = request.getSession().getServletContext().getContextPath();
        return url.delete(url.length() - request.getRequestURI().length(), url.length()).append(contextPath).toString();
    }
}
