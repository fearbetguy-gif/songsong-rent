package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("rent_bill")
public class RentBill {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("lease_id")
    private Long leaseId;

    @TableField("billing_month")
    private String billingMonth;

    private BigDecimal amount;

    @TableField("due_date")
    private LocalDate dueDate;

    /**
     * 0-待支付,1-已支付,2-已逾期
     */
    private Integer status;

    @TableField("paid_time")
    private LocalDateTime paidTime;

    @TableField("payment_method")
    private String paymentMethod;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
