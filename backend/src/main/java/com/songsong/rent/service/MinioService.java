package com.songsong.rent.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    String uploadImage(MultipartFile file);
}
