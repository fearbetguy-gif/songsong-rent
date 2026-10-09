package com.songsong.rent.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HouseAppointmentVO {

    private Long id;
    private Long houseId;
    private String houseTitle;
    private String houseCoverImage;

    private Long userId;
    private String username;
    private String nickname;

    private String viewerName;
    private String phone;
    private LocalDateTime appointmentTime;
    private String remark;

    private Integer status;
    private String rejectReason;
    private Long operatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

