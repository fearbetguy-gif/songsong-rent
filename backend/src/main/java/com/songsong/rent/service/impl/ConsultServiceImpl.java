package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.ConsultMessageSendRequest;
import com.songsong.rent.entity.ConsultMessage;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.User;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.ConsultMessageMapper;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.UserMapper;
import com.songsong.rent.service.ConsultService;
import com.songsong.rent.vo.ConsultMessageVO;
import com.songsong.rent.vo.ConsultSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultServiceImpl implements ConsultService {

    private final ConsultMessageMapper consultMessageMapper;
    private final HouseMapper houseMapper;
    private final UserMapper userMapper;

    @Override
    public void sendToLandlord(Long userId, ConsultMessageSendRequest request) {
        User sender = requireUser(userId);
        House house = requireHouse(request.getHouseId());

        if (Objects.equals(house.getCreatorId(), userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己发布的房源发咨询消息");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能为空");
        }

        insertMessage(house.getId(), sender.getId(), house.getCreatorId(), request.getContent().trim());
    }

    @Override
    public List<ConsultMessageVO> listHouseMessages(Long userId, Long houseId) {
        requireUser(userId);
        House house = requireHouse(houseId);
        if (!Objects.equals(house.getCreatorId(), userId)) {
            // tenant 侧：只能看自己和房东之间的消息
            return listHouseMessagesForPair(houseId, userId, house.getCreatorId());
        }

        // landlord 侧传 houseId 时，默认返回最近会话（若有）避免越权；详细会话通过 listHouseMessagesForPair 路径走 landlordReply 场景
        LambdaQueryWrapper<ConsultMessage> latestQuery = new LambdaQueryWrapper<>();
        latestQuery.eq(ConsultMessage::getHouseId, houseId)
                .orderByDesc(ConsultMessage::getCreateTime, ConsultMessage::getId)
                .last("limit 1");
        ConsultMessage latest = consultMessageMapper.selectOne(latestQuery);
        if (latest == null) {
            return List.of();
        }
        Long tenantId = Objects.equals(latest.getFromUserId(), userId) ? latest.getToUserId() : latest.getFromUserId();
        return listHouseMessagesForPair(houseId, tenantId, userId);
    }

    @Override
    public List<ConsultSessionVO> listLandlordSessions(Long landlordId) {
        requireUser(landlordId);

        LambdaQueryWrapper<ConsultMessage> query = new LambdaQueryWrapper<>();
        query.and(w -> w.eq(ConsultMessage::getFromUserId, landlordId).or().eq(ConsultMessage::getToUserId, landlordId))
                .orderByDesc(ConsultMessage::getCreateTime, ConsultMessage::getId);

        List<ConsultMessage> messages = consultMessageMapper.selectList(query);
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        Map<String, ConsultMessage> latestBySession = new LinkedHashMap<>();
        for (ConsultMessage msg : messages) {
            House house = houseMapper.selectById(msg.getHouseId());
            if (house == null || !Objects.equals(house.getCreatorId(), landlordId)) {
                continue;
            }
            Long tenantId = Objects.equals(msg.getFromUserId(), landlordId) ? msg.getToUserId() : msg.getFromUserId();
            String sessionKey = msg.getHouseId() + "_" + tenantId;
            latestBySession.putIfAbsent(sessionKey, msg);
        }

        if (latestBySession.isEmpty()) {
            return List.of();
        }

        List<Long> houseIds = latestBySession.values().stream().map(ConsultMessage::getHouseId).distinct().toList();
        Map<Long, House> houseMap = houseMapper.selectBatchIds(houseIds).stream()
                .filter(Objects::nonNull)
                .filter(h -> h.getId() != null)
                .collect(Collectors.toMap(House::getId, h -> h, (a, b) -> a));

        List<Long> userIds = new ArrayList<>();
        userIds.add(landlordId);
        for (ConsultMessage msg : latestBySession.values()) {
            userIds.add(msg.getFromUserId());
            userIds.add(msg.getToUserId());
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds.stream().filter(Objects::nonNull).distinct().toList()).stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return latestBySession.values().stream()
                .map(msg -> {
                    Long tenantId = Objects.equals(msg.getFromUserId(), landlordId) ? msg.getToUserId() : msg.getFromUserId();
                    User landlord = userMap.get(landlordId);
                    User tenant = userMap.get(tenantId);
                    House house = houseMap.get(msg.getHouseId());

                    ConsultSessionVO session = new ConsultSessionVO();
                    session.setHouseId(msg.getHouseId());
                    session.setHouseTitle(house != null ? house.getTitle() : null);
                    session.setLandlordId(landlordId);
                    if (landlord != null) {
                        session.setLandlordUsername(landlord.getUsername());
                        session.setLandlordNickname(landlord.getNickname());
                    }
                    session.setTenantId(tenantId);
                    if (tenant != null) {
                        session.setTenantUsername(tenant.getUsername());
                        session.setTenantNickname(tenant.getNickname());
                    }
                    session.setLastMessage(msg.getContent());
                    session.setLastMessageTime(msg.getCreateTime());
                    return session;
                })
                .sorted(Comparator.comparing(ConsultSessionVO::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<ConsultMessageVO> listLandlordMessages(Long landlordId, Long houseId, Long tenantId) {
        requireUser(landlordId);
        requireUser(tenantId);
        House house = requireHouse(houseId);
        if (!Objects.equals(house.getCreatorId(), landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅可查看自己房源的咨询");
        }
        return listHouseMessagesForPair(houseId, tenantId, landlordId);
    }

    @Override
    public void landlordReply(Long landlordId, Long houseId, Long tenantId, String content) {
        requireUser(landlordId);
        requireUser(tenantId);
        House house = requireHouse(houseId);

        if (!Objects.equals(house.getCreatorId(), landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅可回复自己房源的咨询");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息内容不能为空");
        }

        insertMessage(houseId, landlordId, tenantId, content.trim());
    }

    private List<ConsultMessageVO> listHouseMessagesForPair(Long houseId, Long tenantId, Long landlordId) {
        LambdaQueryWrapper<ConsultMessage> query = new LambdaQueryWrapper<>();
        query.eq(ConsultMessage::getHouseId, houseId)
                .and(w -> w
                        .and(x -> x.eq(ConsultMessage::getFromUserId, tenantId).eq(ConsultMessage::getToUserId, landlordId))
                        .or(x -> x.eq(ConsultMessage::getFromUserId, landlordId).eq(ConsultMessage::getToUserId, tenantId))
                )
                .orderByAsc(ConsultMessage::getCreateTime, ConsultMessage::getId);

        List<ConsultMessage> messages = consultMessageMapper.selectList(query);
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = messages.stream()
                .flatMap(m -> List.of(m.getFromUserId(), m.getToUserId()).stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return messages.stream().map(msg -> {
            ConsultMessageVO vo = new ConsultMessageVO();
            vo.setId(msg.getId());
            vo.setHouseId(msg.getHouseId());
            vo.setFromUserId(msg.getFromUserId());
            vo.setToUserId(msg.getToUserId());
            vo.setContent(msg.getContent());
            vo.setCreateTime(msg.getCreateTime());

            User fromUser = userMap.get(msg.getFromUserId());
            if (fromUser != null) {
                vo.setFromUsername(fromUser.getUsername());
                vo.setFromNickname(fromUser.getNickname());
            }
            User toUser = userMap.get(msg.getToUserId());
            if (toUser != null) {
                vo.setToUsername(toUser.getUsername());
                vo.setToNickname(toUser.getNickname());
            }
            return vo;
        }).toList();
    }

    private void insertMessage(Long houseId, Long fromUserId, Long toUserId, String content) {
        ConsultMessage message = new ConsultMessage();
        message.setHouseId(houseId);
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        consultMessageMapper.insert(message);
    }

    private User requireUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在，请重新登录");
        }
        return user;
    }

    private House requireHouse(Long houseId) {
        if (houseId == null || houseId <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "houseId 非法");
        }
        House house = houseMapper.selectById(houseId);
        if (house == null || house.getStatus() == null || house.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        if (house.getCreatorId() == null || house.getCreatorId() <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该房源未绑定房东账号");
        }
        return house;
    }
}
