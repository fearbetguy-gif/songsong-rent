package com.songsong.rent.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RentBillVO {

    private Long id;
    private Long leaseId;
    private Long houseId;
    private String houseTitle;

    private Long tenantId;
    private String tenantUsername;
    private String tenantNickname;

    private String billingMonth;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Integer status;
    private LocalDateTime paidTime;
    private String paymentMethod;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
