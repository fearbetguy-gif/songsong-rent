package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.entity.CityInfo;
import com.songsong.rent.entity.DistrictInfo;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseImage;
import com.songsong.rent.entity.LabelInfo;
import com.songsong.rent.entity.RoomLabel;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.CityInfoMapper;
import com.songsong.rent.mapper.DistrictInfoMapper;
import com.songsong.rent.mapper.HouseImageMapper;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.LabelInfoMapper;
import com.songsong.rent.mapper.RoomLabelMapper;
import com.songsong.rent.service.HouseService;
import com.songsong.rent.vo.HouseDetailVO;
import com.songsong.rent.vo.HouseListVO;
import com.songsong.rent.vo.PlatformStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseServiceImpl implements HouseService {

    private final HouseMapper houseMapper;
    private final HouseImageMapper houseImageMapper;
    private final RoomLabelMapper roomLabelMapper;
    private final LabelInfoMapper labelInfoMapper;
    private final CityInfoMapper cityInfoMapper;
    private final DistrictInfoMapper districtInfoMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOUSE_DETAIL_CACHE_PREFIX = "house:detail:";

    @Override
    public PageResult<HouseListVO> list(Long pageNum, Long pageSize) {
        Page<House> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(
                House::getId,
                House::getTitle,
                House::getCityId,
                House::getDistrictId,
                House::getAreaName,
                House::getLatitude,
                House::getLongitude,
                House::getRentPrice,
                House::getRoomType,
                House::getAreaSize,
                House::getCoverImage,
                House::getStatus,
                House::getUpdateTime
        );
        queryWrapper.eq(House::getStatus, 1)
                .orderByDesc(House::getUpdateTime);
        Page<House> resultPage = houseMapper.selectPage(page, queryWrapper);
        List<HouseListVO> records = new ArrayList<>();
        for (House house : resultPage.getRecords()) {
            HouseListVO vo = new HouseListVO();
            vo.setId(house.getId());
            vo.setTitle(house.getTitle());
            vo.setRentPrice(house.getRentPrice());
            vo.setCity(resolveCityName(house.getCityId()));
            vo.setDistrict(resolveDistrictName(house.getDistrictId()));
            vo.setAreaName(house.getAreaName());
            vo.setLatitude(house.getLatitude());
            vo.setLongitude(house.getLongitude());
            vo.setRoomType(house.getRoomType());
            vo.setAreaSize(house.getAreaSize());
            vo.setCoverImage(resolveCoverImage(house));
            records.add(vo);
        }
        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public HouseDetailVO detail(Long id) {
        String cacheKey = HOUSE_DETAIL_CACHE_PREFIX + id;
        Object cachedObj = null;
        try {
            // 1. 先查缓存
            cachedObj = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            // Redis连接失败等异常，降级到数据库查询
        }
        
        if (cachedObj instanceof HouseDetailVO) {
            return (HouseDetailVO) cachedObj;
        }

        // 2. 缓存没有，查数据库
        LambdaQueryWrapper<House> houseQuery = new LambdaQueryWrapper<>();
        houseQuery.eq(House::getId, id).select(
                House::getId,
                House::getTitle,
                House::getAddress,
                House::getAreaName,
                House::getRentPrice,
                House::getRoomType,
                House::getAreaSize,
                House::getFloorInfo,
                House::getOrientation,
                House::getDecoration,
                House::getHouseType,
                House::getRentType,
                House::getDescription,
                House::getLatitude,
                House::getLongitude,
                House::getStatus,
                House::getPublishStatus,
                House::getAuditStatus,
                House::getCoverImage,
                House::getCreatorId,
                House::getCreateTime,
                House::getUpdateTime
        );
        House house = houseMapper.selectOne(houseQuery);
        if (house == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        LambdaQueryWrapper<HouseImage> imageQuery = new LambdaQueryWrapper<>();
        imageQuery.eq(HouseImage::getHouseId, id).orderByAsc(HouseImage::getSortNum, HouseImage::getId);
        List<HouseImage> images = houseImageMapper.selectList(imageQuery);

        LambdaQueryWrapper<RoomLabel> labelRelQuery = new LambdaQueryWrapper<>();
        labelRelQuery.eq(RoomLabel::getRoomId, id);
        List<RoomLabel> roomLabels = roomLabelMapper.selectList(labelRelQuery);
        List<LabelInfo> labels = new ArrayList<>();
        if (!roomLabels.isEmpty()) {
            List<Long> labelIds = roomLabels.stream().map(RoomLabel::getLabelId).toList();
            List<LabelInfo> labelInfos = labelInfoMapper.selectBatchIds(labelIds);
            if (labelInfos != null) {
                labels = new ArrayList<>(labelInfos);
                labels.sort(Comparator.comparing(LabelInfo::getSortNum, Comparator.nullsLast(Comparator.naturalOrder())));
            }
        }

        fillCityDistrictNames(house);

        HouseDetailVO detailVO = new HouseDetailVO();
        detailVO.setHouse(house);
        detailVO.setImages(images);
        detailVO.setLabels(labels);

        // 3. 放入缓存并设置过期时间（如5分钟），防止缓存雪崩
        try {
            redisTemplate.opsForValue().set(cacheKey, detailVO, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis写入失败不影响业务
        }

        return detailVO;
    }

    @Override
    public void deleteById(Long id) {
        LambdaUpdateWrapper<House> uw = new LambdaUpdateWrapper<>();
        uw.eq(House::getId, id).set(House::getStatus, 0);
        int rows = houseMapper.update(null, uw);
        if (rows == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HouseAddRequest request) {
        DistrictInfo district = districtInfoMapper.selectById(request.getDistrict());
        if (district == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "区域不存在");
        }
        House house = new House();
        house.setTitle(request.getTitle());
        house.setRentPrice(request.getRentPrice());
        house.setDistrictId(request.getDistrict());
        house.setCityId(district.getCityId());
        house.setAddress("-");
        house.setDescription(request.getDescription());
        house.setLatitude(request.getLatitude());
        house.setLongitude(request.getLongitude());
        houseMapper.insert(house);
        syncHouseLabels(house.getId(), request.getLabelIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(HouseUpdateRequest request) {
        House existHouse = houseMapper.selectById(request.getId());
        if (existHouse == null || existHouse.getStatus() == null || existHouse.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        existHouse.setTitle(request.getTitle().trim());
        existHouse.setRentPrice(request.getRentPrice());
        existHouse.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        existHouse.setLatitude(request.getLatitude());
        existHouse.setLongitude(request.getLongitude());
        houseMapper.updateById(existHouse);
        syncHouseLabels(existHouse.getId(), request.getLabelIds());

        // 删除缓存
        try {
            redisTemplate.delete(HOUSE_DETAIL_CACHE_PREFIX + request.getId());
        } catch (Exception e) {
            // Redis删除失败不影响主流程
        }
    }

    @Override
    public PlatformStatsVO stats() {
        PlatformStatsVO vo = new PlatformStatsVO();

        LambdaQueryWrapper<House> houseCountQuery = new LambdaQueryWrapper<>();
        houseCountQuery.eq(House::getStatus, 1);
        vo.setHouseCount(houseMapper.selectCount(houseCountQuery));

        try {
            vo.setCityCount(cityInfoMapper.selectCount(new QueryWrapper<>()));
        } catch (Exception ex) {
            vo.setCityCount(0L);
        }
        try {
            vo.setDistrictCount(districtInfoMapper.selectCount(new QueryWrapper<>()));
        } catch (Exception ex) {
            vo.setDistrictCount(0L);
        }

        QueryWrapper<House> avgQuery = new QueryWrapper<>();
        avgQuery.select("ifnull(avg(rent_price), 0) as avg_price");
        List<Map<String, Object>> rows = houseMapper.selectMaps(avgQuery);
        Object avgValue = rows.isEmpty() ? null : rows.get(0).get("avg_price");
        if (avgValue == null) {
            vo.setAvgRentPrice(java.math.BigDecimal.ZERO);
        } else {
            vo.setAvgRentPrice(new java.math.BigDecimal(String.valueOf(avgValue)));
        }
        return vo;
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

    private String resolveCityName(Long cityId) {
        if (cityId == null) {
            return null;
        }
        CityInfo city = cityInfoMapper.selectById(cityId);
        return city != null ? city.getName() : null;
    }

    private String resolveDistrictName(Long districtId) {
        if (districtId == null) {
            return null;
        }
        DistrictInfo district = districtInfoMapper.selectById(districtId);
        return district != null ? district.getName() : null;
    }

    private void fillCityDistrictNames(House house) {
        house.setCity(resolveCityName(house.getCityId()));
        house.setDistrict(resolveDistrictName(house.getDistrictId()));
    }

    private void syncHouseLabels(Long houseId, List<Long> labelIds) {
        LambdaQueryWrapper<RoomLabel> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(RoomLabel::getRoomId, houseId);
        roomLabelMapper.delete(deleteQuery);

        if (labelIds == null || labelIds.isEmpty()) {
            return;
        }

        List<Long> distinctLabelIds = labelIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctLabelIds.isEmpty()) {
            return;
        }

        List<LabelInfo> labels = labelInfoMapper.selectBatchIds(distinctLabelIds);
        if (labels == null || labels.size() != distinctLabelIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "标签不存在");
        }

        for (Long labelId : distinctLabelIds) {
            RoomLabel roomLabel = new RoomLabel();
            roomLabel.setRoomId(houseId);
            roomLabel.setLabelId(labelId);
            roomLabelMapper.insert(roomLabel);
        }
    }
}
