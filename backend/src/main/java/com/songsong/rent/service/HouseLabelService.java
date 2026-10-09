package com.songsong.rent.service;

import com.songsong.rent.dto.HouseLabelAddRequest;
import com.songsong.rent.entity.RoomLabel;

import java.util.List;

public interface HouseLabelService {

    List<RoomLabel> list(Long houseId);

    void add(HouseLabelAddRequest request);

    void deleteById(Long id);
}
