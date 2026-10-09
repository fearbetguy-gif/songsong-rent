package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseAppointmentAddRequest;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseAppointment;
import com.songsong.rent.entity.User;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.HouseAppointmentMapper;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.UserMapper;
import com.songsong.rent.service.HouseAppointmentService;
import com.songsong.rent.vo.HouseAppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseAppointmentServiceImpl implements HouseAppointmentService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_CONFIRMED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_CANCELED = 4;

    private final HouseAppointmentMapper houseAppointmentMapper;
    private final HouseMapper houseMapper;
    private final UserMapper userMapper;

    @Override
    public void add(Long userId, HouseAppointmentAddRequest request) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在，请重新登录");
        }
        House house = houseMapper.selectById(request.getHouseId());
        if (house == null || house.getStatus() == null || house.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        if (request.getAppointmentTime() == null || request.getAppointmentTime().isBefore(LocalDateTime.now().plusMinutes(10))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "预约时间不能早于当前时间（至少提前10分钟）");
        }
        if (!StringUtils.hasText(request.getViewerName()) || !StringUtils.hasText(request.getPhone())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "看房人姓名/手机号不能为空");
        }

        HouseAppointment appointment = new HouseAppointment();
        appointment.setHouseId(request.getHouseId());
        appointment.setUserId(userId);
        appointment.setViewerName(request.getViewerName().trim());
        appointment.setPhone(request.getPhone().trim());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        appointment.setStatus(STATUS_PENDING);
        houseAppointmentMapper.insert(appointment);
    }

    @Override
    public PageResult<HouseAppointmentVO> myList(Long userId, Integer status, Long pageNum, Long pageSize) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        Page<HouseAppointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HouseAppointment> qw = new LambdaQueryWrapper<>();
        qw.eq(HouseAppointment::getUserId, userId);
        if (status != null) {
            qw.eq(HouseAppointment::getStatus, status);
        }
        qw.orderByDesc(HouseAppointment::getCreateTime, HouseAppointment::getId);

        Page<HouseAppointment> resultPage = houseAppointmentMapper.selectPage(page, qw);
        List<HouseAppointment> records = resultPage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, House> houseMap = fetchHouseMap(records);
        User user = userMapper.selectById(userId);

        List<HouseAppointmentVO> appointmentViewList = records.stream().map(appointment -> {
            HouseAppointmentVO appointmentView = toVO(appointment);
            House house = houseMap.get(appointment.getHouseId());
            if (house != null) {
                appointmentView.setHouseTitle(house.getTitle());
                appointmentView.setHouseCoverImage(house.getCoverImage());
            }
            if (user != null) {
                appointmentView.setUsername(user.getUsername());
                appointmentView.setNickname(user.getNickname());
            }
            return appointmentView;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, appointmentViewList);
    }

    @Override
    public void cancel(Long userId, Long appointmentId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        HouseAppointment appointment = houseAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约记录不存在");
        }
        if (!Objects.equals(appointment.getUserId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能取消自己的预约");
        }
        if (appointment.getStatus() == null || (appointment.getStatus() != STATUS_PENDING && appointment.getStatus() != STATUS_CONFIRMED)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不可取消");
        }

        LambdaUpdateWrapper<HouseAppointment> uw = new LambdaUpdateWrapper<>();
        uw.eq(HouseAppointment::getId, appointmentId)
                .eq(HouseAppointment::getUserId, userId)
                .in(HouseAppointment::getStatus, STATUS_PENDING, STATUS_CONFIRMED)
                .set(HouseAppointment::getStatus, STATUS_CANCELED);
        int rows = houseAppointmentMapper.update(null, uw);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "取消失败，请刷新后重试");
        }
    }

    @Override
    public PageResult<HouseAppointmentVO> adminList(Integer status, Long houseId, Long pageNum, Long pageSize) {
        Page<HouseAppointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HouseAppointment> qw = new LambdaQueryWrapper<>();
        if (status != null) {
            qw.eq(HouseAppointment::getStatus, status);
        }
        if (houseId != null) {
            qw.eq(HouseAppointment::getHouseId, houseId);
        }
        qw.orderByDesc(HouseAppointment::getCreateTime, HouseAppointment::getId);

        Page<HouseAppointment> resultPage = houseAppointmentMapper.selectPage(page, qw);
        List<HouseAppointment> records = resultPage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, House> houseMap = fetchHouseMap(records);
        Map<Long, User> userMap = fetchUserMap(records);

        List<HouseAppointmentVO> appointmentViewList = records.stream().map(appointment -> {
            HouseAppointmentVO appointmentView = toVO(appointment);
            House house = houseMap.get(appointment.getHouseId());
            if (house != null) {
                appointmentView.setHouseTitle(house.getTitle());
                appointmentView.setHouseCoverImage(house.getCoverImage());
            }
            User user = userMap.get(appointment.getUserId());
            if (user != null) {
                appointmentView.setUsername(user.getUsername());
                appointmentView.setNickname(user.getNickname());
            }
            return appointmentView;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, appointmentViewList);
    }

    @Override
    public void adminConfirm(Long appointmentId, Long operatorId) {
        updateStatusOrThrow(appointmentId, operatorId, STATUS_PENDING, STATUS_CONFIRMED, null);
    }

    @Override
    public void adminReject(Long appointmentId, Long operatorId, String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        updateStatusOrThrow(appointmentId, operatorId, STATUS_PENDING, STATUS_REJECTED, reason.trim());
    }

    @Override
    public void adminFinish(Long appointmentId, Long operatorId) {
        updateStatusOrThrow(appointmentId, operatorId, STATUS_CONFIRMED, STATUS_FINISHED, null);
    }

    private void updateStatusOrThrow(Long appointmentId, Long operatorId, int fromStatus, int toStatus, String rejectReason) {
        HouseAppointment appointment = houseAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约记录不存在");
        }
        if (appointment.getStatus() == null || appointment.getStatus() != fromStatus) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不可执行该操作");
        }

        LambdaUpdateWrapper<HouseAppointment> uw = new LambdaUpdateWrapper<>();
        uw.eq(HouseAppointment::getId, appointmentId)
                .eq(HouseAppointment::getStatus, fromStatus)
                .set(HouseAppointment::getStatus, toStatus)
                .set(HouseAppointment::getOperatorId, operatorId);
        if (rejectReason != null) {
            uw.set(HouseAppointment::getRejectReason, rejectReason);
        }
        int rows = houseAppointmentMapper.update(null, uw);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "操作失败，请刷新后重试");
        }
    }

    private HouseAppointmentVO toVO(HouseAppointment appointment) {
        HouseAppointmentVO appointmentView = new HouseAppointmentVO();
        appointmentView.setId(appointment.getId());
        appointmentView.setHouseId(appointment.getHouseId());
        appointmentView.setUserId(appointment.getUserId());
        appointmentView.setViewerName(appointment.getViewerName());
        appointmentView.setPhone(appointment.getPhone());
        appointmentView.setAppointmentTime(appointment.getAppointmentTime());
        appointmentView.setRemark(appointment.getRemark());
        appointmentView.setStatus(appointment.getStatus());
        appointmentView.setRejectReason(appointment.getRejectReason());
        appointmentView.setOperatorId(appointment.getOperatorId());
        appointmentView.setCreateTime(appointment.getCreateTime());
        appointmentView.setUpdateTime(appointment.getUpdateTime());
        return appointmentView;
    }

    private Map<Long, House> fetchHouseMap(List<HouseAppointment> records) {
        List<Long> houseIds = records.stream().map(HouseAppointment::getHouseId).filter(Objects::nonNull).distinct().toList();
        if (houseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<House> houses = houseMapper.selectBatchIds(houseIds);
        if (houses == null || houses.isEmpty()) {
            return Collections.emptyMap();
        }
        return houses.stream().filter(h -> h.getId() != null).collect(Collectors.toMap(House::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, User> fetchUserMap(List<HouseAppointment> records) {
        List<Long> userIds = records.stream().map(HouseAppointment::getUserId).filter(Objects::nonNull).distinct().toList();
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        if (users == null || users.isEmpty()) {
            return Collections.emptyMap();
        }
        return users.stream().filter(u -> u.getId() != null).collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }
}
