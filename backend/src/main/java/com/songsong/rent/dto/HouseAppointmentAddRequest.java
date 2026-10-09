package com.songsong.rent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HouseAppointmentAddRequest {

    @NotNull(message = "houseId 不能为空")
    @Min(value = 1, message = "houseId 不能小于 1")
    private Long houseId;

    @NotBlank(message = "看房人姓名不能为空")
    private String viewerName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;

    private String remark;
}

