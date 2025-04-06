package com.olive.service.storage;

import com.olive.base.utils.uuid.UUID;
import com.olive.model.properties.StorageProperties;
import com.olive.service.util.SpringUtils;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Minio 文件服务
 *
 * @author jhlz
 * @version x.x.x
 */
public class MinioService implements FileStorageService {

    private final MinioClient client = SpringUtils.getBean(MinioClient.class);
    private final StorageProperties storageProperties = SpringUtils.getBean(StorageProperties.class);

    @Override
    public String upload(MultipartFile file) {
        try {

            String newFilename = UUID.fastUUID().toString();
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.minio().defaultBucket())
                            // .object()
                            .stream(file.getInputStream(), file.getSize(), 0)
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

        if (Objects.isNull(bucketName)) {
            return upload(file);
        }
        if (Objects.equals(storageProperties.minio().defaultBucket(), bucketName)) {
            try {
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .stream(file.getInputStream(), file.getSize(), 0)
                        .build()
                );
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .stream(file.getInputStream(), file.getSize(), 0)
                        .build()
                );
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        }

        return "";
    }

}
