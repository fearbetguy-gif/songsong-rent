package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HouseImageUpdateRequest {

    @NotNull(message = "图片ID不能为空")
    @Min(value = 1, message = "图片ID不能小于1")
    private Long id;

    @NotNull(message = "房源ID不能为空")
    @Min(value = 1, message = "房源ID不能小于1")
    private Long houseId;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private Integer sortNum;

    private Integer isCover;
}
