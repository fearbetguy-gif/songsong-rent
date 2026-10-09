package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.DistrictAddRequest;
import com.songsong.rent.dto.DistrictUpdateRequest;
import com.songsong.rent.entity.DistrictInfo;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.DistrictInfoMapper;
import com.songsong.rent.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictInfoMapper districtInfoMapper;

    @Override
    public List<DistrictInfo> list(Long cityId) {
        LambdaQueryWrapper<DistrictInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(cityId != null, DistrictInfo::getCityId, cityId).orderByAsc(DistrictInfo::getId);
        return districtInfoMapper.selectList(qw);
    }

    @Override
    public void add(DistrictAddRequest request) {
        DistrictInfo districtInfo = new DistrictInfo();
        districtInfo.setCityId(request.getCityId());
        districtInfo.setName(request.getName().trim());
        districtInfoMapper.insert(districtInfo);
    }

    @Override
    public void update(DistrictUpdateRequest request) {
        DistrictInfo districtInfo = districtInfoMapper.selectById(request.getId());
        if (districtInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "区域不存在");
        }
        districtInfo.setCityId(request.getCityId());
        districtInfo.setName(request.getName().trim());
        districtInfoMapper.updateById(districtInfo);
    }

    @Override
    public void deleteById(Long id) {
        districtInfoMapper.deleteById(id);
    }
}
