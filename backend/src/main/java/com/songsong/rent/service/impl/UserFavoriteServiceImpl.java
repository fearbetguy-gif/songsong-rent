package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songsong.rent.common.PageResult;
import com.songsong.rent.entity.DistrictInfo;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseImage;
import com.songsong.rent.entity.UserFavorite;
import com.songsong.rent.mapper.DistrictInfoMapper;
import com.songsong.rent.mapper.HouseImageMapper;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.UserFavoriteMapper;
import com.songsong.rent.service.UserFavoriteService;
import com.songsong.rent.vo.HouseListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final HouseMapper houseMapper;
    private final HouseImageMapper houseImageMapper;
    private final DistrictInfoMapper districtInfoMapper;

    private String resolveDistrictName(Long districtId) {
        if (districtId == null) return null;
        DistrictInfo d = districtInfoMapper.selectById(districtId);
        return d != null ? d.getName() : null;
    }

    private String resolveCoverImage(House house) {
        LambdaQueryWrapper<HouseImage> imageQuery = new LambdaQueryWrapper<>();
        imageQuery.eq(HouseImage::getHouseId, house.getId())
                .eq(HouseImage::getIsCover, 1)
                .orderByAsc(HouseImage::getSortNum, HouseImage::getId)
                .last("limit 1");
        HouseImage cover = houseImageMapper.selectOne(imageQuery);
        if (cover == null) {
            LambdaQueryWrapper<HouseImage> anyImageQuery = new LambdaQueryWrapper<>();
            anyImageQuery.eq(HouseImage::getHouseId, house.getId())
                    .orderByAsc(HouseImage::getSortNum, HouseImage::getId)
                    .last("limit 1");
            cover = houseImageMapper.selectOne(anyImageQuery);
        }
        if (cover != null && StringUtils.hasText(cover.getImageUrl())) {
            return cover.getImageUrl();
        }
        if (StringUtils.hasText(house.getCoverImage())) {
            return house.getCoverImage();
        }
        return "";
    }

    @Override
    public void addFavorite(Long userId, Long houseId) {
        if (checkFavorite(userId, houseId)) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setHouseId(houseId);
        favorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long houseId) {
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId)
             .eq(UserFavorite::getHouseId, houseId);
        userFavoriteMapper.delete(query);
    }

    @Override
    public boolean checkFavorite(Long userId, Long houseId) {
        if (userId == null) return false;
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId)
             .eq(UserFavorite::getHouseId, houseId);
        return userFavoriteMapper.exists(query);
    }

    @Override
    public PageResult<HouseListVO> listFavorites(Long userId, Long pageNum, Long pageSize) {
        Page<UserFavorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId)
             .orderByDesc(UserFavorite::getCreateTime);
        userFavoriteMapper.selectPage(page, query);

        List<HouseListVO> voList = new ArrayList<>();
        if (!page.getRecords().isEmpty()) {
            List<Long> houseIds = page.getRecords().stream().map(UserFavorite::getHouseId).collect(Collectors.toList());
            List<House> houses = houseMapper.selectBatchIds(houseIds);
            // 保持收藏时间倒序
            for (UserFavorite fav : page.getRecords()) {
                houses.stream().filter(h -> h.getId().equals(fav.getHouseId())).findFirst().ifPresent(h -> {
                    HouseListVO vo = new HouseListVO();
                    vo.setId(h.getId());
                    vo.setTitle(h.getTitle());
                    vo.setCoverImage(resolveCoverImage(h));
                    vo.setRentPrice(h.getRentPrice());
                    vo.setRoomType(h.getRoomType());
                    vo.setAreaName(h.getAreaName());
                    vo.setAreaSize(h.getAreaSize());
                    vo.setDistrict(resolveDistrictName(h.getDistrictId()));
                    vo.setLatitude(h.getLatitude());
                    vo.setLongitude(h.getLongitude());
                    voList.add(vo);
                });
            }
        }

        PageResult<HouseListVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(voList);
        return result;
    }
}
