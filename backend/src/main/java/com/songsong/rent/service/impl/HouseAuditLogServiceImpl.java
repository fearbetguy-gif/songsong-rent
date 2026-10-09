package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseAuditLogAddRequest;
import com.songsong.rent.dto.HouseAuditLogUpdateRequest;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseAuditLog;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.HouseAuditLogMapper;
import com.songsong.rent.service.HouseAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseAuditLogServiceImpl implements HouseAuditLogService {

    private final HouseAuditLogMapper houseAuditLogMapper;
    private final HouseMapper houseMapper;

    @Override
    public List<HouseAuditLog> list(Long houseId) {
        LambdaQueryWrapper<HouseAuditLog> qw = new LambdaQueryWrapper<>();
        qw.eq(houseId != null, HouseAuditLog::getHouseId, houseId)
                .orderByDesc(HouseAuditLog::getId);
        return houseAuditLogMapper.selectList(qw);
    }

    @Override
    public void add(HouseAuditLogAddRequest request) {
        House house = houseMapper.selectById(request.getHouseId());
        if (house == null || house.getStatus() == null || house.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        HouseAuditLog log = new HouseAuditLog();
        log.setHouseId(request.getHouseId());
        log.setFromStatus(request.getFromStatus() == null ? house.getAuditStatus() : request.getFromStatus());
        log.setToStatus(request.getToStatus());
        log.setOperatorId(request.getOperatorId());
        log.setRemark(request.getRemark());
        houseAuditLogMapper.insert(log);
        applyAuditStatusToHouse(house, request.getToStatus());
        houseMapper.updateById(house);
    }

    @Override
    public void update(HouseAuditLogUpdateRequest request) {
        HouseAuditLog log = houseAuditLogMapper.selectById(request.getId());
        if (log == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审核记录不存在");
        }
        House house = houseMapper.selectById(request.getHouseId());
        if (house == null || house.getStatus() == null || house.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        log.setHouseId(request.getHouseId());
        log.setFromStatus(request.getFromStatus() == null ? house.getAuditStatus() : request.getFromStatus());
        log.setToStatus(request.getToStatus());
        log.setOperatorId(request.getOperatorId());
        log.setRemark(request.getRemark());
        houseAuditLogMapper.updateById(log);
        applyAuditStatusToHouse(house, request.getToStatus());
        houseMapper.updateById(house);
    }

    @Override
    public void deleteById(Long id) {
        houseAuditLogMapper.deleteById(id);
    }

    private void applyAuditStatusToHouse(House house, Integer toStatus) {
        house.setAuditStatus(toStatus);
        if (toStatus != null && toStatus == 1) {
            house.setPublishStatus(1);
        } else {
            house.setPublishStatus(0);
        }
    }
}
