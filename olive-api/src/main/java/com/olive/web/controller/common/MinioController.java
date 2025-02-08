package com.olive.web.controller.common;

import com.olive.framework.annotation.Anonymous;
import com.olive.framework.util.file.FileUtils;
import com.olive.framework.minio.MinioFileVO;
import com.olive.framework.minio.MinioUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Headers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @GetMapping("/{client}")
    @Anonymous
    public void downLoadFile(HttpServletRequest request, HttpServletResponse response,
                             @PathVariable("client") String client,
                             @RequestParam("fileName") String fileName) throws Exception {
        MinioFileVO file = MinioUtil.getFile(client, fileName);
        Headers headers = file.getHeaders();
        String contentType = headers.get("content-Type");
        response.setContentType(contentType);
        FileUtils.writeBytes(file.getFileInputSteam(), response.getOutputStream());
    }

    @PutMapping("/{client}")
    @Anonymous
    public String uploadFile(HttpServletRequest request, HttpServletResponse response,
                             @PathVariable("client") String client, @RequestPart MultipartFile file) throws Exception {
        return MinioUtil.uploadFile(client, file);
    }
}
