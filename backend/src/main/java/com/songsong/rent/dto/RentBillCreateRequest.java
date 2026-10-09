package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentBillCreateRequest {

    @NotNull(message = "leaseId 不能为空")
    @Min(value = 1, message = "leaseId 不能小于 1")
    private Long leaseId;

    @NotBlank(message = "账期不能为空")
    private String billingMonth;

    @NotNull(message = "账单金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "应付日期不能为空")
    private LocalDate dueDate;

    private String remark;
}
