package com.songsong.rent.service;

import com.songsong.rent.dto.CityAddRequest;
import com.songsong.rent.dto.CityUpdateRequest;
import com.songsong.rent.entity.CityInfo;

import java.util.List;

public interface CityService {

    List<CityInfo> list();

    void add(CityAddRequest request);

    void update(CityUpdateRequest request);

    void deleteById(Long id);
}
