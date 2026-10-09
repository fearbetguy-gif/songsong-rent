package com.songsong.rent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID不能小于1")
    private Long id;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotNull(message = "管理员标识不能为空")
    @Min(value = 0, message = "管理员标识只能是0或1")
    @Max(value = 1, message = "管理员标识只能是0或1")
    private Integer isAdmin;
}
