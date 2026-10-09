package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseLabelAddRequest;
import com.songsong.rent.entity.RoomLabel;
import com.songsong.rent.service.HouseLabelService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "房源标签管理")
@Validated
@RestController
@RequestMapping("/house-label")
@RequiredArgsConstructor
public class HouseLabelController {

    private final HouseLabelService houseLabelService;

    @Operation(summary = "房源标签关联列表")
    @GetMapping("/list")
    public Result<List<RoomLabel>> list(@RequestParam(required = false) Long houseId) {
        return Result.success(houseLabelService.list(houseId));
    }

    @Operation(summary = "新增房源标签关联")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid HouseLabelAddRequest request) {
        houseLabelService.add(request);
        return Result.success();
    }

    @Operation(summary = "删除房源标签关联")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        houseLabelService.deleteById(id);
        return Result.success();
    }
}
