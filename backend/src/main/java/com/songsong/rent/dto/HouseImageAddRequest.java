package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HouseImageAddRequest {

    @NotNull(message = "房源ID不能为空")
    private Long houseId;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private Integer sortNum;

    private Integer isCover;
}
