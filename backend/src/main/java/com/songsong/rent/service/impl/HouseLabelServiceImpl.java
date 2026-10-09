package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.dto.HouseLabelAddRequest;
import com.songsong.rent.entity.RoomLabel;
import com.songsong.rent.mapper.RoomLabelMapper;
import com.songsong.rent.service.HouseLabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseLabelServiceImpl implements HouseLabelService {

    private static final String HOUSE_DETAIL_CACHE_PREFIX = "house:detail:";

    private final RoomLabelMapper roomLabelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<RoomLabel> list(Long houseId) {
        LambdaQueryWrapper<RoomLabel> qw = new LambdaQueryWrapper<>();
        qw.eq(houseId != null, RoomLabel::getRoomId, houseId)
                .orderByDesc(RoomLabel::getId);
        return roomLabelMapper.selectList(qw);
    }

    @Override
    public void add(HouseLabelAddRequest request) {
        RoomLabel roomLabel = new RoomLabel();
        roomLabel.setRoomId(request.getHouseId());
        roomLabel.setLabelId(request.getLabelId());
        roomLabelMapper.insert(roomLabel);
        evictHouseDetailCache(request.getHouseId());
    }

    @Override
    public void deleteById(Long id) {
        RoomLabel roomLabel = roomLabelMapper.selectById(id);
        roomLabelMapper.deleteById(id);
        if (roomLabel != null) {
            evictHouseDetailCache(roomLabel.getRoomId());
        }
    }

    private void evictHouseDetailCache(Long houseId) {
        if (houseId == null) {
            return;
        }
        try {
            redisTemplate.delete(HOUSE_DETAIL_CACHE_PREFIX + houseId);
        } catch (Exception e) {
            // Redis 删除失败不影响主流程
        }
    }
}
