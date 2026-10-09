package com.songsong.rent.service;

import com.songsong.rent.dto.DistrictAddRequest;
import com.songsong.rent.dto.DistrictUpdateRequest;
import com.songsong.rent.entity.DistrictInfo;

import java.util.List;

public interface DistrictService {

    List<DistrictInfo> list(Long cityId);

    void add(DistrictAddRequest request);

    void update(DistrictUpdateRequest request);

    void deleteById(Long id);
}
