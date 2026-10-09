package com.songsong.rent.service;

import com.songsong.rent.dto.ConsultMessageSendRequest;
import com.songsong.rent.vo.ConsultMessageVO;
import com.songsong.rent.vo.ConsultSessionVO;

import java.util.List;

public interface ConsultService {

    void sendToLandlord(Long userId, ConsultMessageSendRequest request);

    List<ConsultMessageVO> listHouseMessages(Long userId, Long houseId);

    List<ConsultSessionVO> listLandlordSessions(Long landlordId);

    List<ConsultMessageVO> listLandlordMessages(Long landlordId, Long houseId, Long tenantId);

    void landlordReply(Long landlordId, Long houseId, Long tenantId, String content);
}
