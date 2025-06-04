package com.olive.api.common;

import com.olive.service.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/file/minio")
public class MinioController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public String upload(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestPart MultipartFile file) throws Exception {
        return fileStorageService.upload(file);
    }
}
