package com.songsong.rent.controller;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseAppointmentRejectRequest;
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

@Tag(name = "预约看房（后台管理）")
@Validated
@RestController
@RequestMapping("/appointment-admin")
@RequiredArgsConstructor
public class HouseAppointmentAdminController {

    private final HouseAppointmentService houseAppointmentService;

    @Operation(summary = "预约列表（分页）")
    @GetMapping("/list")
    public Result<PageResult<HouseAppointmentVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long houseId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        return Result.success(houseAppointmentService.adminList(status, houseId, pageNum, pageSize));
    }

    @Operation(summary = "确认预约")
    @PostMapping("/confirm/{id}")
    public Result<Void> confirm(
            HttpServletRequest req,
            @PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id
    ) {
        Long operatorId = (Long) req.getAttribute("adminUserId");
        houseAppointmentService.adminConfirm(id, operatorId);
        return Result.success();
    }

    @Operation(summary = "拒绝预约")
    @PostMapping("/reject/{id}")
    public Result<Void> reject(
            HttpServletRequest req,
            @PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id,
            @RequestBody @Valid HouseAppointmentRejectRequest request
    ) {
        Long operatorId = (Long) req.getAttribute("adminUserId");
        houseAppointmentService.adminReject(id, operatorId, request.getReason());
        return Result.success();
    }

    @Operation(summary = "完成预约")
    @PostMapping("/finish/{id}")
    public Result<Void> finish(
            HttpServletRequest req,
            @PathVariable("id") @Min(value = 1, message = "id 不能小于 1") Long id
    ) {
        Long operatorId = (Long) req.getAttribute("adminUserId");
        houseAppointmentService.adminFinish(id, operatorId);
        return Result.success();
    }
}

