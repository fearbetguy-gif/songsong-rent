package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseAddRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "租金不能为空")
    @DecimalMin(value = "0.01", message = "租金必须大于0")
    private BigDecimal rentPrice;

    @NotNull(message = "区域不能为空")
    private Long district;

    private String description;

    private Double latitude;

    private Double longitude;

    private List<Long> labelIds;
}
