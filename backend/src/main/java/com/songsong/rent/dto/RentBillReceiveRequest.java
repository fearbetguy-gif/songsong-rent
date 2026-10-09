package com.songsong.rent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RentBillReceiveRequest {

    @NotBlank(message = "收款方式不能为空")
    private String paymentMethod;

    private String remark;
}
