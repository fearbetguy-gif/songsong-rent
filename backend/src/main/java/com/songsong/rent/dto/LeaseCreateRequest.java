package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeaseCreateRequest {

    @NotNull(message = "houseId 不能为空")
    @Min(value = 1, message = "houseId 不能小于 1")
    private Long houseId;

    @NotNull(message = "tenantId 不能为空")
    @Min(value = 1, message = "tenantId 不能小于 1")
    private Long tenantId;

    @NotNull(message = "月租金不能为空")
    private BigDecimal monthlyRent;

    @NotNull(message = "租约开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "租约结束日期不能为空")
    private LocalDate endDate;

    private String remark;
}
