package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabelAddRequest {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private Integer sortNum;
}
