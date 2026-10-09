package com.songsong.rent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseAppointmentRejectRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.dto.LeaseCreateRequest;
import com.songsong.rent.dto.RentBillCreateRequest;
import com.songsong.rent.dto.RentBillReceiveRequest;
import com.songsong.rent.entity.DistrictInfo;
import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseAppointment;
import com.songsong.rent.entity.HousePriceHistory;
import com.songsong.rent.entity.LabelInfo;
import com.songsong.rent.entity.LeaseContract;
import com.songsong.rent.entity.RentBill;
import com.songsong.rent.entity.RoomLabel;
import com.songsong.rent.entity.User;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.DistrictInfoMapper;
import com.songsong.rent.mapper.HouseAppointmentMapper;
import com.songsong.rent.mapper.HouseMapper;
import com.songsong.rent.mapper.HousePriceHistoryMapper;
import com.songsong.rent.mapper.LabelInfoMapper;
import com.songsong.rent.mapper.LeaseContractMapper;
import com.songsong.rent.mapper.RentBillMapper;
import com.songsong.rent.mapper.RoomLabelMapper;
import com.songsong.rent.mapper.UserMapper;
import com.songsong.rent.service.LandlordService;
import com.songsong.rent.vo.HouseAppointmentVO;
import com.songsong.rent.vo.LandlordHouseVO;
import com.songsong.rent.vo.LeaseContractVO;
import com.songsong.rent.vo.RentBillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandlordServiceImpl implements LandlordService {

    private static final String HOUSE_DETAIL_CACHE_PREFIX = "house:detail:";

    private static final int HOUSE_STATUS_ACTIVE = 1;
    private static final int HOUSE_STATUS_REMOVED = 0;

    private static final int APPOINTMENT_PENDING = 0;
    private static final int APPOINTMENT_CONFIRMED = 1;
    private static final int APPOINTMENT_REJECTED = 2;
    private static final int APPOINTMENT_FINISHED = 3;

    private static final int LEASE_PENDING = 0;
    private static final int LEASE_ACTIVE = 1;
    private static final int LEASE_FINISHED = 2;
    private static final int LEASE_TERMINATED = 3;

    private static final int BILL_PENDING = 0;
    private static final int BILL_PAID = 1;
    private static final int BILL_OVERDUE = 2;

    private final HouseMapper houseMapper;
    private final DistrictInfoMapper districtInfoMapper;
    private final HouseAppointmentMapper houseAppointmentMapper;
    private final UserMapper userMapper;
    private final LeaseContractMapper leaseContractMapper;
    private final RentBillMapper rentBillMapper;
    private final RoomLabelMapper roomLabelMapper;
    private final LabelInfoMapper labelInfoMapper;
    private final HousePriceHistoryMapper housePriceHistoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<LandlordHouseVO> listMyHouses(Long landlordId, Long pageNum, Long pageSize) {
        requireValidLandlord(landlordId);

        Page<House> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<House> query = new LambdaQueryWrapper<>();
        query.eq(House::getCreatorId, landlordId)
                .orderByDesc(House::getUpdateTime, House::getId);

        Page<House> resultPage = houseMapper.selectPage(page, query);
        List<House> houses = resultPage.getRecords();
        if (houses == null || houses.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, String> districtNameMap = buildDistrictNameMap(houses);
        Map<Long, List<LabelInfo>> labelsMap = buildHouseLabelsMap(houses);

        List<LandlordHouseVO> houseViews = houses.stream().map(house -> {
            LandlordHouseVO view = new LandlordHouseVO();
            view.setId(house.getId());
            view.setTitle(house.getTitle());
            view.setRentPrice(house.getRentPrice());
            view.setDistrict(districtNameMap.get(house.getDistrictId()));
            view.setDescription(house.getDescription());
            view.setLabels(labelsMap.getOrDefault(house.getId(), Collections.emptyList()));
            view.setStatus(house.getStatus());
            view.setPublishStatus(house.getPublishStatus());
            view.setAuditStatus(house.getAuditStatus());
            view.setCreateTime(house.getCreateTime());
            view.setUpdateTime(house.getUpdateTime());
            return view;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, houseViews);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHouse(Long landlordId, HouseAddRequest request) {
        requireValidLandlord(landlordId);

        DistrictInfo district = districtInfoMapper.selectById(request.getDistrict());
        if (district == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "区域不存在");
        }

        House house = new House();
        house.setTitle(request.getTitle().trim());
        house.setRentPrice(request.getRentPrice());
        house.setDistrictId(request.getDistrict());
        house.setCityId(district.getCityId());
        house.setAddress("-");
        house.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        house.setLatitude(request.getLatitude());
        house.setLongitude(request.getLongitude());
        house.setStatus(HOUSE_STATUS_ACTIVE);
        house.setPublishStatus(1);
        house.setAuditStatus(0);
        house.setCreatorId(landlordId);
        houseMapper.insert(house);
        syncHouseLabels(house.getId(), request.getLabelIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHouse(Long landlordId, HouseUpdateRequest request) {
        requireValidLandlord(landlordId);

        House house = checkHouseOwnership(landlordId, request.getId());
        if (house.getStatus() == null || house.getStatus() == HOUSE_STATUS_REMOVED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }

        java.math.BigDecimal oldPrice = house.getRentPrice();
        java.math.BigDecimal newPrice = request.getRentPrice();
        house.setTitle(request.getTitle().trim());
        house.setRentPrice(newPrice);
        house.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        house.setLatitude(request.getLatitude());
        house.setLongitude(request.getLongitude());
        houseMapper.updateById(house);
        recordPriceHistoryIfChanged(house.getId(), oldPrice, newPrice, landlordId);
        syncHouseLabels(house.getId(), request.getLabelIds());
        evictHouseDetailCache(house.getId());
    }

    @Override
    public void removeHouse(Long landlordId, Long houseId) {
        requireValidLandlord(landlordId);
        checkHouseOwnership(landlordId, houseId);

        LambdaUpdateWrapper<House> update = new LambdaUpdateWrapper<>();
        update.eq(House::getId, houseId)
                .eq(House::getCreatorId, landlordId)
                .set(House::getStatus, HOUSE_STATUS_REMOVED);

        int rows = houseMapper.update(null, update);
        if (rows == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在或无权限");
        }
    }

    @Override
    public PageResult<HousePriceHistory> listHousePriceHistories(Long landlordId, Long houseId, Long pageNum, Long pageSize) {
        requireValidLandlord(landlordId);
        checkHouseOwnership(landlordId, houseId);

        Page<HousePriceHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HousePriceHistory> query = new LambdaQueryWrapper<>();
        query.eq(HousePriceHistory::getHouseId, houseId)
                .orderByDesc(HousePriceHistory::getCreateTime, HousePriceHistory::getId);
        Page<HousePriceHistory> resultPage = housePriceHistoryMapper.selectPage(page, query);
        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, resultPage.getRecords());
    }

    @Override
    public PageResult<HouseAppointmentVO> listMyHouseAppointments(Long landlordId, Integer status, Long pageNum, Long pageSize) {
        requireValidLandlord(landlordId);

        List<Long> myHouseIds = listMyHouseIds(landlordId);
        if (myHouseIds.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Page<HouseAppointment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HouseAppointment> query = new LambdaQueryWrapper<>();
        query.in(HouseAppointment::getHouseId, myHouseIds);
        if (status != null) {
            query.eq(HouseAppointment::getStatus, status);
        }
        query.orderByDesc(HouseAppointment::getCreateTime, HouseAppointment::getId);

        Page<HouseAppointment> resultPage = houseAppointmentMapper.selectPage(page, query);
        List<HouseAppointment> appointments = resultPage.getRecords();
        if (appointments == null || appointments.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, House> houseMap = houseMapper.selectBatchIds(myHouseIds).stream()
                .filter(Objects::nonNull)
                .filter(h -> h.getId() != null)
                .collect(Collectors.toMap(House::getId, Function.identity(), (a, b) -> a));

        List<Long> userIds = appointments.stream()
                .map(HouseAppointment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<HouseAppointmentVO> appointmentViews = appointments.stream().map(appointment -> {
            HouseAppointmentVO view = new HouseAppointmentVO();
            view.setId(appointment.getId());
            view.setHouseId(appointment.getHouseId());
            view.setUserId(appointment.getUserId());
            view.setViewerName(appointment.getViewerName());
            view.setPhone(appointment.getPhone());
            view.setAppointmentTime(appointment.getAppointmentTime());
            view.setRemark(appointment.getRemark());
            view.setStatus(appointment.getStatus());
            view.setRejectReason(appointment.getRejectReason());
            view.setOperatorId(appointment.getOperatorId());
            view.setCreateTime(appointment.getCreateTime());
            view.setUpdateTime(appointment.getUpdateTime());

            House house = houseMap.get(appointment.getHouseId());
            if (house != null) {
                view.setHouseTitle(house.getTitle());
                view.setHouseCoverImage(house.getCoverImage());
            }
            User tenant = userMap.get(appointment.getUserId());
            if (tenant != null) {
                view.setUsername(tenant.getUsername());
                view.setNickname(tenant.getNickname());
            }
            return view;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, appointmentViews);
    }

    @Override
    public void confirmAppointment(Long landlordId, Long appointmentId) {
        updateAppointmentStatus(landlordId, appointmentId, APPOINTMENT_PENDING, APPOINTMENT_CONFIRMED, null);
    }

    @Override
    public void rejectAppointment(Long landlordId, Long appointmentId, HouseAppointmentRejectRequest request) {
        if (request == null || !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        updateAppointmentStatus(landlordId, appointmentId, APPOINTMENT_PENDING, APPOINTMENT_REJECTED, request.getReason().trim());
    }

    @Override
    public void finishAppointment(Long landlordId, Long appointmentId) {
        updateAppointmentStatus(landlordId, appointmentId, APPOINTMENT_CONFIRMED, APPOINTMENT_FINISHED, null);
    }

    @Override
    public void createLease(Long landlordId, LeaseCreateRequest request) {
        requireValidLandlord(landlordId);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "租约开始日期不能晚于结束日期");
        }

        House house = checkHouseOwnership(landlordId, request.getHouseId());
        if (house.getStatus() == null || house.getStatus() == HOUSE_STATUS_REMOVED) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "房源已下架，无法创建租约");
        }

        User tenant = userMapper.selectById(request.getTenantId());
        if (tenant == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "租客不存在");
        }

        LeaseContract lease = new LeaseContract();
        lease.setHouseId(request.getHouseId());
        lease.setLandlordId(landlordId);
        lease.setTenantId(request.getTenantId());
        lease.setMonthlyRent(request.getMonthlyRent());
        lease.setStartDate(request.getStartDate());
        lease.setEndDate(request.getEndDate());
        lease.setStatus(LEASE_ACTIVE);
        lease.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        leaseContractMapper.insert(lease);
    }

    @Override
    public PageResult<LeaseContractVO> listLeases(Long landlordId, Integer status, Long pageNum, Long pageSize) {
        requireValidLandlord(landlordId);

        Page<LeaseContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LeaseContract> query = new LambdaQueryWrapper<>();
        query.eq(LeaseContract::getLandlordId, landlordId);
        if (status != null) {
            query.eq(LeaseContract::getStatus, status);
        }
        query.orderByDesc(LeaseContract::getCreateTime, LeaseContract::getId);

        Page<LeaseContract> resultPage = leaseContractMapper.selectPage(page, query);
        List<LeaseContract> leases = resultPage.getRecords();
        if (leases == null || leases.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, House> houseMap = houseMapper.selectBatchIds(
                        leases.stream().map(LeaseContract::getHouseId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(Objects::nonNull)
                .filter(h -> h.getId() != null)
                .collect(Collectors.toMap(House::getId, Function.identity(), (a, b) -> a));

        Map<Long, User> tenantMap = userMapper.selectBatchIds(
                        leases.stream().map(LeaseContract::getTenantId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<LeaseContractVO> records = leases.stream().map(lease -> {
            LeaseContractVO vo = new LeaseContractVO();
            vo.setId(lease.getId());
            vo.setHouseId(lease.getHouseId());
            vo.setTenantId(lease.getTenantId());
            vo.setMonthlyRent(lease.getMonthlyRent());
            vo.setStartDate(lease.getStartDate());
            vo.setEndDate(lease.getEndDate());
            vo.setStatus(lease.getStatus());
            vo.setRemark(lease.getRemark());
            vo.setCreateTime(lease.getCreateTime());
            vo.setUpdateTime(lease.getUpdateTime());

            House house = houseMap.get(lease.getHouseId());
            if (house != null) {
                vo.setHouseTitle(house.getTitle());
            }
            User tenant = tenantMap.get(lease.getTenantId());
            if (tenant != null) {
                vo.setTenantUsername(tenant.getUsername());
                vo.setTenantNickname(tenant.getNickname());
            }
            return vo;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public void createRentBill(Long landlordId, RentBillCreateRequest request) {
        requireValidLandlord(landlordId);

        LeaseContract lease = leaseContractMapper.selectById(request.getLeaseId());
        if (lease == null || !Objects.equals(lease.getLandlordId(), landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅可为自己的租约创建账单");
        }
        if (lease.getStatus() == null || (lease.getStatus() != LEASE_ACTIVE && lease.getStatus() != LEASE_PENDING)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前租约状态不可创建账单");
        }

        LambdaQueryWrapper<RentBill> existsQuery = new LambdaQueryWrapper<>();
        existsQuery.eq(RentBill::getLeaseId, request.getLeaseId())
                .eq(RentBill::getBillingMonth, request.getBillingMonth().trim())
                .last("limit 1");
        RentBill exists = rentBillMapper.selectOne(existsQuery);
        if (exists != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该账期账单已存在");
        }

        RentBill bill = new RentBill();
        bill.setLeaseId(request.getLeaseId());
        bill.setBillingMonth(request.getBillingMonth().trim());
        bill.setAmount(request.getAmount());
        bill.setDueDate(request.getDueDate());
        bill.setStatus(request.getDueDate().isBefore(java.time.LocalDate.now()) ? BILL_OVERDUE : BILL_PENDING);
        bill.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        rentBillMapper.insert(bill);
    }

    @Override
    public PageResult<RentBillVO> listRentBills(Long landlordId, Integer status, Long pageNum, Long pageSize) {
        requireValidLandlord(landlordId);

        List<Long> leaseIds = listMyLeaseIds(landlordId);
        if (leaseIds.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Page<RentBill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RentBill> query = new LambdaQueryWrapper<>();
        query.in(RentBill::getLeaseId, leaseIds);
        if (status != null) {
            query.eq(RentBill::getStatus, status);
        }
        query.orderByDesc(RentBill::getCreateTime, RentBill::getId);

        Page<RentBill> resultPage = rentBillMapper.selectPage(page, query);
        List<RentBill> bills = resultPage.getRecords();
        if (bills == null || bills.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        Map<Long, LeaseContract> leaseMap = leaseContractMapper.selectBatchIds(
                        bills.stream().map(RentBill::getLeaseId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(Objects::nonNull)
                .filter(l -> l.getId() != null)
                .collect(Collectors.toMap(LeaseContract::getId, Function.identity(), (a, b) -> a));

        Map<Long, House> houseMap = houseMapper.selectBatchIds(
                        leaseMap.values().stream().map(LeaseContract::getHouseId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(Objects::nonNull)
                .filter(h -> h.getId() != null)
                .collect(Collectors.toMap(House::getId, Function.identity(), (a, b) -> a));

        Map<Long, User> tenantMap = userMapper.selectBatchIds(
                        leaseMap.values().stream().map(LeaseContract::getTenantId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<RentBillVO> records = bills.stream().map(bill -> {
            RentBillVO vo = new RentBillVO();
            vo.setId(bill.getId());
            vo.setLeaseId(bill.getLeaseId());
            vo.setBillingMonth(bill.getBillingMonth());
            vo.setAmount(bill.getAmount());
            vo.setDueDate(bill.getDueDate());
            vo.setStatus(bill.getStatus());
            vo.setPaidTime(bill.getPaidTime());
            vo.setPaymentMethod(bill.getPaymentMethod());
            vo.setRemark(bill.getRemark());
            vo.setCreateTime(bill.getCreateTime());
            vo.setUpdateTime(bill.getUpdateTime());

            LeaseContract lease = leaseMap.get(bill.getLeaseId());
            if (lease != null) {
                vo.setHouseId(lease.getHouseId());
                vo.setTenantId(lease.getTenantId());
                House house = houseMap.get(lease.getHouseId());
                if (house != null) {
                    vo.setHouseTitle(house.getTitle());
                }
                User tenant = tenantMap.get(lease.getTenantId());
                if (tenant != null) {
                    vo.setTenantUsername(tenant.getUsername());
                    vo.setTenantNickname(tenant.getNickname());
                }
            }
            return vo;
        }).toList();

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public void markBillReceived(Long landlordId, Long billId, RentBillReceiveRequest request) {
        requireValidLandlord(landlordId);
        if (request == null || !StringUtils.hasText(request.getPaymentMethod())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收款方式不能为空");
        }

        RentBill bill = rentBillMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账单不存在");
        }

        LeaseContract lease = leaseContractMapper.selectById(bill.getLeaseId());
        if (lease == null || !Objects.equals(lease.getLandlordId(), landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该账单");
        }
        if (bill.getStatus() != null && bill.getStatus() == BILL_PAID) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该账单已收款");
        }

        LambdaUpdateWrapper<RentBill> update = new LambdaUpdateWrapper<>();
        update.eq(RentBill::getId, billId)
                .set(RentBill::getStatus, BILL_PAID)
                .set(RentBill::getPaidTime, LocalDateTime.now())
                .set(RentBill::getPaymentMethod, request.getPaymentMethod().trim())
                .set(RentBill::getRemark, StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : bill.getRemark());

        int rows = rentBillMapper.update(null, update);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收款状态更新失败");
        }
    }

    private void updateAppointmentStatus(Long landlordId, Long appointmentId, int fromStatus, int toStatus, String rejectReason) {
        requireValidLandlord(landlordId);

        HouseAppointment appointment = houseAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预约记录不存在");
        }
        checkHouseOwnership(landlordId, appointment.getHouseId());

        if (appointment.getStatus() == null || appointment.getStatus() != fromStatus) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不可执行该操作");
        }

        LambdaUpdateWrapper<HouseAppointment> update = new LambdaUpdateWrapper<>();
        update.eq(HouseAppointment::getId, appointmentId)
                .eq(HouseAppointment::getStatus, fromStatus)
                .set(HouseAppointment::getStatus, toStatus)
                .set(HouseAppointment::getOperatorId, landlordId);
        if (rejectReason != null) {
            update.set(HouseAppointment::getRejectReason, rejectReason);
        }

        int rows = houseAppointmentMapper.update(null, update);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "操作失败，请刷新后重试");
        }
    }

    private House checkHouseOwnership(Long landlordId, Long houseId) {
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房源不存在");
        }
        if (!Objects.equals(house.getCreatorId(), landlordId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅可管理自己发布的房源");
        }
        return house;
    }

    private List<Long> listMyHouseIds(Long landlordId) {
        LambdaQueryWrapper<House> query = new LambdaQueryWrapper<>();
        query.select(House::getId).eq(House::getCreatorId, landlordId);
        List<House> houses = houseMapper.selectList(query);
        if (houses == null || houses.isEmpty()) {
            return Collections.emptyList();
        }
        return houses.stream().map(House::getId).filter(Objects::nonNull).toList();
    }

    private List<Long> listMyLeaseIds(Long landlordId) {
        LambdaQueryWrapper<LeaseContract> query = new LambdaQueryWrapper<>();
        query.select(LeaseContract::getId).eq(LeaseContract::getLandlordId, landlordId);
        List<LeaseContract> leases = leaseContractMapper.selectList(query);
        if (leases == null || leases.isEmpty()) {
            return Collections.emptyList();
        }
        return leases.stream().map(LeaseContract::getId).filter(Objects::nonNull).toList();
    }

    private Map<Long, String> buildDistrictNameMap(List<House> houses) {
        List<Long> districtIds = houses.stream()
                .map(House::getDistrictId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (districtIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return districtInfoMapper.selectBatchIds(districtIds).stream()
                .filter(Objects::nonNull)
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(DistrictInfo::getId, DistrictInfo::getName, (a, b) -> a));
    }

    private Map<Long, List<LabelInfo>> buildHouseLabelsMap(List<House> houses) {
        List<Long> houseIds = houses.stream()
                .map(House::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (houseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<RoomLabel> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(RoomLabel::getRoomId, houseIds);
        List<RoomLabel> relations = roomLabelMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> labelIds = relations.stream()
                .map(RoomLabel::getLabelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (labelIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, LabelInfo> labelMap = labelInfoMapper.selectBatchIds(labelIds).stream()
                .filter(Objects::nonNull)
                .filter(label -> label.getId() != null)
                .collect(Collectors.toMap(LabelInfo::getId, Function.identity(), (a, b) -> a));

        return relations.stream()
                .filter(relation -> relation.getRoomId() != null && relation.getLabelId() != null)
                .filter(relation -> labelMap.containsKey(relation.getLabelId()))
                .collect(Collectors.groupingBy(
                        RoomLabel::getRoomId,
                        Collectors.collectingAndThen(
                                Collectors.mapping(relation -> labelMap.get(relation.getLabelId()), Collectors.toList()),
                                labels -> labels.stream()
                                        .distinct()
                                        .sorted(Comparator.comparing(LabelInfo::getSortNum, Comparator.nullsLast(Comparator.naturalOrder()))
                                                .thenComparing(LabelInfo::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                                        .toList()
                        )
                ));
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

    private void evictHouseDetailCache(Long houseId) {
        try {
            redisTemplate.delete(HOUSE_DETAIL_CACHE_PREFIX + houseId);
        } catch (Exception e) {
            // Redis 删除失败不影响主流程
        }
    }

    private void recordPriceHistoryIfChanged(Long houseId, java.math.BigDecimal oldPrice, java.math.BigDecimal newPrice, Long operatorId) {
        if (oldPrice == null || newPrice == null || oldPrice.compareTo(newPrice) == 0) {
            return;
        }
        HousePriceHistory history = new HousePriceHistory();
        history.setHouseId(houseId);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        history.setOperatorId(operatorId);
        history.setEffectiveTime(LocalDateTime.now());
        housePriceHistoryMapper.insert(history);
    }

    private void requireValidLandlord(Long landlordId) {
        if (landlordId == null || landlordId <= 0) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectById(landlordId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在，请重新登录");
        }
    }
}
