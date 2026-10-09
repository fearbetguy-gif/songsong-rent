package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.DistrictAddRequest;
import com.songsong.rent.dto.DistrictUpdateRequest;
import com.songsong.rent.entity.DistrictInfo;
import com.songsong.rent.service.DistrictService;
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

@Tag(name = "区域管理")
@Validated
@RestController
@RequestMapping("/district")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @Operation(summary = "区域列表")
    @GetMapping("/list")
    public Result<List<DistrictInfo>> list(@RequestParam(required = false) Long cityId) {
        return Result.success(districtService.list(cityId));
    }

    @Operation(summary = "新增区域")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid DistrictAddRequest request) {
        districtService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑区域")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid DistrictUpdateRequest request) {
        districtService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除区域")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        districtService.deleteById(id);
        return Result.success();
    }
}
