package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试接口")
@Validated
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "Hello 测试接口")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello songsong-rent");
    }
}
