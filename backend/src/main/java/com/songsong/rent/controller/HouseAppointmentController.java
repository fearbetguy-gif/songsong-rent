package com.songsong.rent.controller;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.Result;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.dto.HouseAppointmentAddRequest;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.service.HouseAppointmentService;
import com.songsong.rent.vo.HouseAppointmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约看房（用户端）")
@Validated
@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class HouseAppointmentController {

    private final HouseAppointmentService houseAppointmentService;

    @Operation(summary = "提交预约看房")
    @PostMapping("/add")
    public Result<Void> add(
            HttpServletRequest req,
            @RequestBody @Valid HouseAppointmentAddRequest request
    ) {
        Long userId = (Long) req.getAttribute("userId");
        houseAppointmentService.add(userId, request);
        return Result.success();
    }

    @Operation(summary = "我的预约列表（分页）")
    @GetMapping("/my-list")
    public Result<PageResult<HouseAppointmentVO>> myList(
            HttpServletRequest req,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(houseAppointmentService.myList(userId, status, pageNum, pageSize));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(
            HttpServletRequest req,
            @PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id
    ) {
        Long userId = (Long) req.getAttribute("userId");
        houseAppointmentService.cancel(userId, id);
        return Result.success();
    }
}

