package com.songsong.rent.service;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseAppointmentRejectRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.dto.LeaseCreateRequest;
import com.songsong.rent.dto.RentBillCreateRequest;
import com.songsong.rent.dto.RentBillReceiveRequest;
import com.songsong.rent.entity.HousePriceHistory;
import com.songsong.rent.vo.HouseAppointmentVO;
import com.songsong.rent.vo.LandlordHouseVO;
import com.songsong.rent.vo.LeaseContractVO;
import com.songsong.rent.vo.RentBillVO;

public interface LandlordService {

    PageResult<LandlordHouseVO> listMyHouses(Long landlordId, Long pageNum, Long pageSize);

    void addHouse(Long landlordId, HouseAddRequest request);

    void updateHouse(Long landlordId, HouseUpdateRequest request);

    void removeHouse(Long landlordId, Long houseId);

    PageResult<HousePriceHistory> listHousePriceHistories(Long landlordId, Long houseId, Long pageNum, Long pageSize);

    PageResult<HouseAppointmentVO> listMyHouseAppointments(Long landlordId, Integer status, Long pageNum, Long pageSize);

    void confirmAppointment(Long landlordId, Long appointmentId);

    void rejectAppointment(Long landlordId, Long appointmentId, HouseAppointmentRejectRequest request);

    void finishAppointment(Long landlordId, Long appointmentId);

    void createLease(Long landlordId, LeaseCreateRequest request);

    PageResult<LeaseContractVO> listLeases(Long landlordId, Integer status, Long pageNum, Long pageSize);

    void createRentBill(Long landlordId, RentBillCreateRequest request);

    PageResult<RentBillVO> listRentBills(Long landlordId, Integer status, Long pageNum, Long pageSize);

    void markBillReceived(Long landlordId, Long billId, RentBillReceiveRequest request);
}
