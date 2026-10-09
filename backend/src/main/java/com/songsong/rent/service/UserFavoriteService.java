package com.songsong.rent.service;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.vo.HouseListVO;

public interface UserFavoriteService {
    void addFavorite(Long userId, Long houseId);
    void removeFavorite(Long userId, Long houseId);
    boolean checkFavorite(Long userId, Long houseId);
    PageResult<HouseListVO> listFavorites(Long userId, Long pageNum, Long pageSize);
}