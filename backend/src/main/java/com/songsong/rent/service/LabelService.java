package com.songsong.rent.service;

import com.songsong.rent.dto.LabelAddRequest;
import com.songsong.rent.dto.LabelUpdateRequest;
import com.songsong.rent.entity.LabelInfo;

import java.util.List;

public interface LabelService {

    List<LabelInfo> list();

    void add(LabelAddRequest request);

    void update(LabelUpdateRequest request);

    void deleteById(Long id);
}
