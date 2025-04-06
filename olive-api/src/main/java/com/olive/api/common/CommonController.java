package com.olive.api.common;

import com.olive.framework.annotation.Anonymous;
import com.olive.framework.config.AppConfig;
import com.olive.service.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @Anonymous
    @PostMapping
    public void pdfTest(MultipartFile file) {
        try (
                RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create("localhost:9200")));
                ){
            // PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(file.getInputStream()));
            // PDFTextStripper pdfTextStripper = new PDFTextStripper();
            // // 文档内容
            // String text = pdfTextStripper.getText(document);
            //
            // XContentBuilder mapping = XContentFactory.jsonBuilder()
            //         .startObject()
            //         .startObject("properties")
            //         .startObject("content")
            //         .field("type", "text")
            //         .field("analyzer", "ik_max_word")
            //         .field("search_analyzer", "ik_smart")
            //         .startObject("fields")
            //         .startObject("keyword")
            //         .field("type", "keyword")
            //         .endObject()
            //         .endObject()
            //         .endObject()
            //         .startObject("title")
            //         .field("type", "keyword")
            //         .endObject()
            //         .endObject()
            //         .endObject();
            //
            //
            // System.out.println(text);
            // Map<String, String> map = Map.of(
            //         "title", file.getOriginalFilename(),
            //         "content", text
            // );
            // CreateIndexResponse response = client.indices()
            //         .create(new CreateIndexRequest("ik_test").settings(
            //                                 Settings.builder()
            //                                         .put("index.number_of_shards", 3)
            //                                         .put("index.number_of_replicas", 1)
            //                                         .build()
            //                         )
            //                         .mapping(mapping)
            //                         .source(map)
            //                 ,
            //                 RequestOptions.DEFAULT);
            //
            // System.out.println(response.toString());

            // new SearchRequest("ik_test", new SearchSourceBuilder().query(
            //         QueryBuilders.matchQuery("content", "数据库监控")
            // ))

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
    @PostMapping("/upload")
    @Anonymous
    public Map<String, Object> uploadFile(@RequestPart MultipartFile file) throws Exception {
        try {
            String uploadPath = fileStorageService.upload(file);


            log.info("file upload path is  {}", uploadPath);
            String fileName = "";
            String url = getUrl() + fileName;
            return Map.of(
                    "url", url,
                    "fileName", fileName,
                    "newFileName", FilenameUtils.getName(fileName),
                    "originalFilename", file.getOriginalFilename()
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
