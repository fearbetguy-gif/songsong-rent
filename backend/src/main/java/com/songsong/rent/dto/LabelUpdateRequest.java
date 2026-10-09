package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LabelUpdateRequest {

    @NotNull(message = "标签ID不能为空")
    @Min(value = 1, message = "标签ID不能小于1")
    private Long id;

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private Integer sortNum;
}
