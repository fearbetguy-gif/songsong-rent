package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.CityAddRequest;
import com.songsong.rent.dto.CityUpdateRequest;
import com.songsong.rent.entity.CityInfo;
import com.songsong.rent.service.CityService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "城市管理")
@Validated
@RestController
@RequestMapping("/city")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(summary = "城市列表")
    @GetMapping("/list")
    public Result<List<CityInfo>> list() {
        return Result.success(cityService.list());
    }

    @Operation(summary = "新增城市")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid CityAddRequest request) {
        cityService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑城市")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid CityUpdateRequest request) {
        cityService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除城市")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        cityService.deleteById(id);
        return Result.success();
    }
}
