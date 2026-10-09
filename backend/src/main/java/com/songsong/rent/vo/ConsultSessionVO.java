package com.songsong.rent.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultSessionVO {

    private Long houseId;
    private String houseTitle;

    private Long tenantId;
    private String tenantUsername;
    private String tenantNickname;

    private Long landlordId;
    private String landlordUsername;
    private String landlordNickname;

    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
