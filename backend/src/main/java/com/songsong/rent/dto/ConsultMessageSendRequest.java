package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsultMessageSendRequest {

    @NotNull(message = "houseId 不能为空")
    @Min(value = 1, message = "houseId 不能小于 1")
    private Long houseId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
