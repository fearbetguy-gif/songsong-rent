package com.songsong.rent.controller;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.service.HouseService;
import com.songsong.rent.vo.HouseDetailVO;
import com.songsong.rent.vo.HouseListVO;
import com.songsong.rent.vo.PlatformStatsVO;
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

@Tag(name = "房源接口")
@Validated
@RestController
@RequestMapping("/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    @Operation(summary = "房源分页列表")
    @GetMapping("/list")
    public Result<PageResult<HouseListVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize) {
        return Result.success(houseService.list(pageNum, pageSize));
    }

    @Operation(summary = "平台数据统计")
    @GetMapping("/stats")
    public Result<PlatformStatsVO> stats() {
        return Result.success(houseService.stats());
    }

    @Operation(summary = "房源详情")
    @GetMapping("/{id}")
    public Result<HouseDetailVO> detail(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        return Result.success(houseService.detail(id));
    }

    @Operation(summary = "删除房源")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        houseService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "新增房源")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid HouseAddRequest request) {
        houseService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑房源")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid HouseUpdateRequest request) {
        houseService.update(request);
        return Result.success();
    }
}
