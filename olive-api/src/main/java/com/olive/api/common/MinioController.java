package com.olive.api.common;

import com.olive.service.annotation.Anonymous;
import com.olive.service.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/file/minio")
public class MinioController {

    private final FileStorageService fileStorageService;

    @Anonymous
    @PostMapping("/upload")
    public String upload(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestPart MultipartFile file) throws Exception {
        return fileStorageService.upload(file);
    }
}
