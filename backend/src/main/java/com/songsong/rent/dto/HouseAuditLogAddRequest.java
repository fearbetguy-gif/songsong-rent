package com.songsong.rent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HouseAuditLogAddRequest {

    @NotNull(message = "房源ID不能为空")
    private Long houseId;

    private Integer fromStatus;

    @NotNull(message = "目标状态不能为空")
    private Integer toStatus;

    private Long operatorId;

    private String remark;
}
