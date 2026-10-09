package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.UserUpdateRequest;
import com.songsong.rent.entity.User;
import com.songsong.rent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户接口")
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid UserRegisterRequest request) {
        return Result.success(userService.register(request.getUsername(), request.getNickname(), request.getPassword()));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<User> login(@RequestBody @Valid UserLoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "后台管理员登录")
    @PostMapping("/admin-login")
    public Result<User> adminLogin(@RequestBody @Valid UserLoginRequest request) {
        return Result.success(userService.adminLogin(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid UserUpdateRequest request) {
        userService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        userService.deleteById(id);
        return Result.success();
    }

    @Data
    public static class UserRegisterRequest {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "昵称不能为空")
        private String nickname;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class UserLoginRequest {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
