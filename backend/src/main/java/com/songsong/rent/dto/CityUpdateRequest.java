package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CityUpdateRequest {

    @NotNull(message = "城市ID不能为空")
    @Min(value = 1, message = "城市ID不能小于1")
    private Long id;

    @NotBlank(message = "城市名称不能为空")
    private String name;
}
