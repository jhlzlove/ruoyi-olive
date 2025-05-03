package com.olive.service.storage;

import com.olive.base.utils.LocalDateUtil;
import com.olive.base.utils.uuid.UUID;
import com.olive.service.util.SpringUtils;
import io.minio.*;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Minio 文件服务
 *
 * @author jhlz
 * @version x.x.x
 */
@AllArgsConstructor
public class MinioService implements FileStorageService {

    private final StorageProperties.MinioProperties properties;
    private final MinioClient client = SpringUtils.getBean(MinioClient.class);

    @Override
    public String upload(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            String newFilename = UUID.fastUUID().toString();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.defaultBucket())
                            // .object()
                            .stream(is, file.getSize(), 0)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
        return "minio";
    }

    @Override
    public String upload(@Nullable String bucketName, MultipartFile file) {
        bucketName = Objects.isNull(bucketName) ? properties.defaultBucket() : bucketName;
        String prefix = LocalDateUtil.toStr(LocalDateUtil.dateTime(), LocalDateUtil.YYYYMMDDHHMMSS);
        String filename;
        String originalFilename = file.getOriginalFilename();
        if (Objects.nonNull(originalFilename) && originalFilename.indexOf('.') > 0) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            filename = prefix + extension;
        } else {
            filename = prefix;
        }
        try (InputStream is = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(is, file.getSize(), 0)
                    .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
        return properties.url() + bucketName + "/" + filename;
    }

    /**
     * 检测桶是否存在，若不存在则创建
     *
     * @param bucket 桶名
     */
    @SneakyThrows
    private void checkBucketExists(String bucket) {
        BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(properties.defaultBucket()).build();
        if (!client.bucketExists(existsArgs)) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(properties.defaultBucket()).build();
            client.makeBucket(makeBucketArgs);
        }
    }



}
