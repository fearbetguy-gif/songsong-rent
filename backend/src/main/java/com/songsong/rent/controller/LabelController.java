package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.LabelAddRequest;
import com.songsong.rent.dto.LabelUpdateRequest;
import com.songsong.rent.entity.LabelInfo;
import com.songsong.rent.service.LabelService;
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

@Tag(name = "标签管理")
@Validated
@RestController
@RequestMapping("/label")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @Operation(summary = "标签列表")
    @GetMapping("/list")
    public Result<List<LabelInfo>> list() {
        return Result.success(labelService.list());
    }

    @Operation(summary = "新增标签")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid LabelAddRequest request) {
        labelService.add(request);
        return Result.success();
    }

    @Operation(summary = "编辑标签")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid LabelUpdateRequest request) {
        labelService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id) {
        labelService.deleteById(id);
        return Result.success();
    }
}
