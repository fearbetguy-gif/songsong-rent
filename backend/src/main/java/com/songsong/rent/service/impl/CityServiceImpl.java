package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.CityAddRequest;
import com.songsong.rent.dto.CityUpdateRequest;
import com.songsong.rent.entity.CityInfo;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.CityInfoMapper;
import com.songsong.rent.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityInfoMapper cityInfoMapper;

    @Override
    public List<CityInfo> list() {
        LambdaQueryWrapper<CityInfo> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(CityInfo::getId);
        return cityInfoMapper.selectList(qw);
    }

    @Override
    public void add(CityAddRequest request) {
        CityInfo cityInfo = new CityInfo();
        cityInfo.setName(request.getName().trim());
        cityInfoMapper.insert(cityInfo);
    }

    @Override
    public void update(CityUpdateRequest request) {
        CityInfo cityInfo = cityInfoMapper.selectById(request.getId());
        if (cityInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "城市不存在");
        }
        cityInfo.setName(request.getName().trim());
        cityInfoMapper.updateById(cityInfo);
    }

    @Override
    public void deleteById(Long id) {
        cityInfoMapper.deleteById(id);
    }
}
