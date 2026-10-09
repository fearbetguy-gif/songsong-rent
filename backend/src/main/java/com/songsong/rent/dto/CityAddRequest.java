package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CityAddRequest {

    @NotBlank(message = "城市名称不能为空")
    private String name;
}
