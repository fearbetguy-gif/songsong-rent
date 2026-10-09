package com.songsong.rent.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HouseLabelAddRequest {

    @NotNull(message = "房源ID不能为空")
    private Long houseId;

    @NotNull(message = "标签ID不能为空")
    private Long labelId;
}
