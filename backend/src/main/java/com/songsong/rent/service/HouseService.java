package com.songsong.rent.service;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.vo.HouseDetailVO;
import com.songsong.rent.vo.HouseListVO;
import com.songsong.rent.vo.PlatformStatsVO;

public interface HouseService {

    PageResult<HouseListVO> list(Long pageNum, Long pageSize);

    HouseDetailVO detail(Long id);

    void deleteById(Long id);

    void add(HouseAddRequest request);

    void update(HouseUpdateRequest request);

    PlatformStatsVO stats();
}
