package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.LabelAddRequest;
import com.songsong.rent.dto.LabelUpdateRequest;
import com.songsong.rent.entity.LabelInfo;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.LabelInfoMapper;
import com.songsong.rent.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelInfoMapper labelInfoMapper;

    @Override
    public List<LabelInfo> list() {
        LambdaQueryWrapper<LabelInfo> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(LabelInfo::getSortNum).orderByAsc(LabelInfo::getId);
        return labelInfoMapper.selectList(qw);
    }

    @Override
    public void add(LabelAddRequest request) {
        LabelInfo labelInfo = new LabelInfo();
        labelInfo.setName(request.getName().trim());
        labelInfo.setSortNum(request.getSortNum() == null ? 0 : request.getSortNum());
        labelInfoMapper.insert(labelInfo);
    }

    @Override
    public void update(LabelUpdateRequest request) {
        LabelInfo labelInfo = labelInfoMapper.selectById(request.getId());
        if (labelInfo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        labelInfo.setName(request.getName().trim());
        labelInfo.setSortNum(request.getSortNum() == null ? 0 : request.getSortNum());
        labelInfoMapper.updateById(labelInfo);
    }

    @Override
    public void deleteById(Long id) {
        labelInfoMapper.deleteById(id);
    }
}
