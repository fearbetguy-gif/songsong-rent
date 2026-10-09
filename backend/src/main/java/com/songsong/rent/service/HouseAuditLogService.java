package com.songsong.rent.service;

import com.songsong.rent.dto.HouseAuditLogAddRequest;
import com.songsong.rent.dto.HouseAuditLogUpdateRequest;
import com.songsong.rent.entity.HouseAuditLog;

import java.util.List;

public interface HouseAuditLogService {

    List<HouseAuditLog> list(Long houseId);

    void add(HouseAuditLogAddRequest request);

    void update(HouseAuditLogUpdateRequest request);

    void deleteById(Long id);
}
