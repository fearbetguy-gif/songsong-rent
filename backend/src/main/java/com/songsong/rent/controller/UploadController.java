package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final MinioService minioService;

    @Operation(summary = "上传图片到MinIO")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file) {
        return Result.success(minioService.uploadImage(file));
    }
}
