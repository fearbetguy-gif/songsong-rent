package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HouseAuditLogUpdateRequest {

    @NotNull(message = "审核记录ID不能为空")
    @Min(value = 1, message = "审核记录ID不能小于1")
    private Long id;

    @NotNull(message = "房源ID不能为空")
    @Min(value = 1, message = "房源ID不能小于1")
    private Long houseId;

    private Integer fromStatus;

    @NotNull(message = "目标状态不能为空")
    private Integer toStatus;

    private Long operatorId;

    private String remark;
}
