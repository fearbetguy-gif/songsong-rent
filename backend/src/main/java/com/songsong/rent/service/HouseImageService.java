package com.songsong.rent.service;

import com.songsong.rent.dto.HouseImageAddRequest;
import com.songsong.rent.dto.HouseImageUpdateRequest;
import com.songsong.rent.entity.HouseImage;

import java.util.List;

public interface HouseImageService {

    List<HouseImage> list(Long houseId);

    void add(HouseImageAddRequest request);

    void update(HouseImageUpdateRequest request);

    void deleteById(Long id);
}
