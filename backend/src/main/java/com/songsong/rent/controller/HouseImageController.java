package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseImageAddRequest;
import com.songsong.rent.dto.HouseImageUpdateRequest;
import com.songsong.rent.entity.HouseImage;
import com.songsong.rent.service.HouseImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "房源图片管理")
@Validated
@RestController
@RequestMapping("/house-image")
@RequiredArgsConstructor
public class HouseImageController {

    private final HouseImageService houseImageService;

    @Operation(summary = "房源图片列表")
    @GetMapping("/list")
    public Result<List<HouseImage>> list(@RequestParam(required = false) Long houseId) {
        return Result.success(houseImageService.list(houseId));
    }

    @Operation(summary = "新增房源图片")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid HouseImageAddRequest request) {
        houseImageService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑房源图片")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid HouseImageUpdateRequest request) {
        houseImageService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除房源图片")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        houseImageService.deleteById(id);
        return Result.success();
    }
}
