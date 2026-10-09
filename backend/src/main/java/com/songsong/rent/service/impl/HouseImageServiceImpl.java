package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseImageAddRequest;
import com.songsong.rent.dto.HouseImageUpdateRequest;
import com.songsong.rent.entity.HouseImage;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.HouseImageMapper;
import com.songsong.rent.service.HouseImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseImageServiceImpl implements HouseImageService {

    private final HouseImageMapper houseImageMapper;

    @Override
    public List<HouseImage> list(Long houseId) {
        LambdaQueryWrapper<HouseImage> qw = new LambdaQueryWrapper<>();
        qw.eq(houseId != null, HouseImage::getHouseId, houseId)
                .orderByAsc(HouseImage::getSortNum)
                .orderByAsc(HouseImage::getId);
        return houseImageMapper.selectList(qw);
    }

    @Override
    public void add(HouseImageAddRequest request) {
        HouseImage houseImage = new HouseImage();
        houseImage.setHouseId(request.getHouseId());
        houseImage.setImageUrl(request.getImageUrl().trim());
        houseImage.setSortNum(request.getSortNum() == null ? 0 : request.getSortNum());
        houseImage.setIsCover(request.getIsCover() == null ? 0 : request.getIsCover());
        houseImageMapper.insert(houseImage);
    }

    @Override
    public void update(HouseImageUpdateRequest request) {
        HouseImage houseImage = houseImageMapper.selectById(request.getId());
        if (houseImage == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图片记录不存在");
        }
        houseImage.setHouseId(request.getHouseId());
        houseImage.setImageUrl(request.getImageUrl().trim());
        houseImage.setSortNum(request.getSortNum() == null ? 0 : request.getSortNum());
        houseImage.setIsCover(request.getIsCover() == null ? 0 : request.getIsCover());
        houseImageMapper.updateById(houseImage);
    }

    @Override
    public void deleteById(Long id) {
        houseImageMapper.deleteById(id);
    }
}
