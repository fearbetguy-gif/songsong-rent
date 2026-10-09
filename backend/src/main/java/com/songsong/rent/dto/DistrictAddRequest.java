package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DistrictAddRequest {

    @NotNull(message = "城市ID不能为空")
    private Long cityId;

    @NotBlank(message = "区域名称不能为空")
    private String name;
}
