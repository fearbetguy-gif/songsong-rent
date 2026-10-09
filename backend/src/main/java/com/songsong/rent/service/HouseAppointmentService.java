package com.songsong.rent.service;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.dto.HouseAppointmentAddRequest;
import com.songsong.rent.vo.HouseAppointmentVO;

public interface HouseAppointmentService {

    void add(Long userId, HouseAppointmentAddRequest request);

    PageResult<HouseAppointmentVO> myList(Long userId, Integer status, Long pageNum, Long pageSize);

    void cancel(Long userId, Long appointmentId);

    PageResult<HouseAppointmentVO> adminList(Integer status, Long houseId, Long pageNum, Long pageSize);

    void adminConfirm(Long appointmentId, Long operatorId);

    void adminReject(Long appointmentId, Long operatorId, String reason);

    void adminFinish(Long appointmentId, Long operatorId);
}

