package com.songsong.rent.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaseContractVO {

    private Long id;
    private Long houseId;
    private String houseTitle;

    private Long tenantId;
    private String tenantUsername;
    private String tenantNickname;

    private BigDecimal monthlyRent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
