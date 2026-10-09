package com.songsong.rent.service.impl;

import com.songsong.rent.common.ResultCode;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        try {
            ensureBucket();
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = "house/" + UUID.randomUUID().toString().replace("-", "") + suffix;
            String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build()
                );
            }
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("MinIO上传失败", ex);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "上传失败:" + ex.getMessage());
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        String publicReadPolicy = "{\n"
                + "  \"Version\": \"2012-10-17\",\n"
                + "  \"Statement\": [\n"
                + "    {\n"
                + "      \"Effect\": \"Allow\",\n"
                + "      \"Principal\": {\"AWS\": [\"*\"]},\n"
                + "      \"Action\": [\"s3:GetBucketLocation\", \"s3:ListBucket\"],\n"
                + "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "\"]\n"
                + "    },\n"
                + "    {\n"
                + "      \"Effect\": \"Allow\",\n"
                + "      \"Principal\": {\"AWS\": [\"*\"]},\n"
                + "      \"Action\": [\"s3:GetObject\"],\n"
                + "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(publicReadPolicy)
                        .build()
        );
    }
}
