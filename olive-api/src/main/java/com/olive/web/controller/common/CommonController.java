package com.olive.web.controller.common;

import com.olive.framework.annotation.Anonymous;
import com.olive.framework.config.AppConfig;
import com.olive.framework.exception.ServiceException;
import com.olive.framework.util.StringUtils;
import com.olive.framework.util.file.FileOperateUtils;
import com.olive.framework.util.file.FileUtils;
import com.olive.framework.util.file.MimeTypeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

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
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = AppConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            // FileUtils.writeBytes(filePath, response.getOutputStream());
            FileOperateUtils.downLoad(filePath, response.getOutputStream());
            if (delete) {
                FileOperateUtils.deleteFile(fileName);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    @Anonymous
    public Map<String, Object> uploadFile(@RequestPart MultipartFile file) throws Exception {
        try {
            // 上传文件路径
            // String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileOperateUtils.upload(file);
            String url = getUrl() + fileName;
            // AjaxResult ajax = AjaxResult.success();
            // ajax.put("url", url);
            // ajax.put("fileName", fileName);
            // ajax.put("newFileName", FileUtils.getName(fileName));
            // ajax.put("originalFilename", file.getOriginalFilename());
            return Map.of(
                    "url", url,
                    "fileName", fileName,
                    "newFileName", FileUtils.getName(fileName),
                    "originalFilename", file.getOriginalFilename()
            );
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
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
                String fileName = FileOperateUtils.upload(filePath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
                String url = getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            // AjaxResult ajax = AjaxResult.success();
            // ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            // ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            // ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            // ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            // return ajax;
            return Map.of(
                    "urls", StringUtils.join(urls, FILE_DELIMETER),
                    "fileNames", StringUtils.join(fileNames, FILE_DELIMETER),
                    "newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER),
                    "originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER)
            );
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    @Anonymous
    public void resourceDownload(@RequestParam String resource,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, resource);
            FileOperateUtils.downLoad(resource, response.getOutputStream());
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
