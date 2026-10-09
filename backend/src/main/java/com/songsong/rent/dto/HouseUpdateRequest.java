package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseUpdateRequest {

    @NotNull(message = "房源ID不能为空")
    @Min(value = 1, message = "房源ID不能小于1")
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "租金不能为空")
    @DecimalMin(value = "0.01", message = "租金必须大于0")
    private BigDecimal rentPrice;

    private String description;

    private Double latitude;

    private Double longitude;

    private List<Long> labelIds;
}
