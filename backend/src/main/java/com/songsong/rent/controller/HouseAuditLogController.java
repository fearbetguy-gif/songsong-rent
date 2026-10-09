package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseAuditLogAddRequest;
import com.songsong.rent.dto.HouseAuditLogUpdateRequest;
import com.songsong.rent.entity.HouseAuditLog;
import com.songsong.rent.service.HouseAuditLogService;
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

@Tag(name = "审核记录管理")
@Validated
@RestController
@RequestMapping("/audit-log")
@RequiredArgsConstructor
public class HouseAuditLogController {

    private final HouseAuditLogService houseAuditLogService;

    @Operation(summary = "审核记录列表")
    @GetMapping("/list")
    public Result<List<HouseAuditLog>> list(@RequestParam(required = false) Long houseId) {
        return Result.success(houseAuditLogService.list(houseId));
    }

    @Operation(summary = "新增审核记录")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid HouseAuditLogAddRequest request) {
        houseAuditLogService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑审核记录")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid HouseAuditLogUpdateRequest request) {
        houseAuditLogService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除审核记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        houseAuditLogService.deleteById(id);
        return Result.success();
    }
}
