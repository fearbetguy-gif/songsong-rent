package com.songsong.rent.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultMessageVO {

    private Long id;
    private Long houseId;

    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;

    private Long toUserId;
    private String toUsername;
    private String toNickname;

    private String content;
    private LocalDateTime createTime;
}
