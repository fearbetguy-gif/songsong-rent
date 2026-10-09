package com.songsong.rent.controller;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.Result;
import com.songsong.rent.dto.HouseAddRequest;
import com.songsong.rent.dto.HouseAppointmentRejectRequest;
import com.songsong.rent.dto.HouseUpdateRequest;
import com.songsong.rent.dto.LeaseCreateRequest;
import com.songsong.rent.dto.RentBillCreateRequest;
import com.songsong.rent.dto.RentBillReceiveRequest;
import com.songsong.rent.entity.HousePriceHistory;
import com.songsong.rent.service.LandlordService;
import com.songsong.rent.vo.HouseAppointmentVO;
import com.songsong.rent.vo.LandlordHouseVO;
import com.songsong.rent.vo.LeaseContractVO;
import com.songsong.rent.vo.RentBillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

@Tag(name = "房东端")
@Validated
@RestController
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class LandlordController {

    private final LandlordService landlordService;

    @Operation(summary = "我的房源列表")
    @GetMapping("/house/list")
    public Result<PageResult<LandlordHouseVO>> listMyHouses(
            HttpServletRequest req,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        return Result.success(landlordService.listMyHouses(landlordId, pageNum, pageSize));
    }

    @Operation(summary = "发布房源")
    @PostMapping("/house/add")
    public Result<Void> addHouse(HttpServletRequest req, @RequestBody @Valid HouseAddRequest request) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.addHouse(landlordId, request);
        return Result.success();
    }

    @Operation(summary = "编辑房源")
    @PutMapping("/house/update")
    public Result<Void> updateHouse(HttpServletRequest req, @RequestBody @Valid HouseUpdateRequest request) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.updateHouse(landlordId, request);
        return Result.success();
    }

    @Operation(summary = "下架房源")
    @DeleteMapping("/house/{houseId}")
    public Result<Void> removeHouse(
            HttpServletRequest req,
            @PathVariable("houseId") @Min(value = 1, message = "houseId 不能小于 1") Long houseId
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.removeHouse(landlordId, houseId);
        return Result.success();
    }

    @Operation(summary = "查看房源价格历史")
    @GetMapping("/house/{houseId}/price-history")
    public Result<PageResult<HousePriceHistory>> listHousePriceHistories(
            HttpServletRequest req,
            @PathVariable("houseId") @Min(value = 1, message = "houseId 不能小于 1") Long houseId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        return Result.success(landlordService.listHousePriceHistories(landlordId, houseId, pageNum, pageSize));
    }

    @Operation(summary = "查看我的房源预约")
    @GetMapping("/appointment/list")
    public Result<PageResult<HouseAppointmentVO>> listMyAppointments(
            HttpServletRequest req,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        return Result.success(landlordService.listMyHouseAppointments(landlordId, status, pageNum, pageSize));
    }

    @Operation(summary = "确认预约")
    @PostMapping("/appointment/confirm/{appointmentId}")
    public Result<Void> confirmAppointment(
            HttpServletRequest req,
            @PathVariable("appointmentId") @Min(value = 1, message = "appointmentId 不能小于 1") Long appointmentId
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.confirmAppointment(landlordId, appointmentId);
        return Result.success();
    }

    @Operation(summary = "拒绝预约")
    @PostMapping("/appointment/reject/{appointmentId}")
    public Result<Void> rejectAppointment(
            HttpServletRequest req,
            @PathVariable("appointmentId") @Min(value = 1, message = "appointmentId 不能小于 1") Long appointmentId,
            @RequestBody @Valid HouseAppointmentRejectRequest request
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.rejectAppointment(landlordId, appointmentId, request);
        return Result.success();
    }

    @Operation(summary = "完成预约")
    @PostMapping("/appointment/finish/{appointmentId}")
    public Result<Void> finishAppointment(
            HttpServletRequest req,
            @PathVariable("appointmentId") @Min(value = 1, message = "appointmentId 不能小于 1") Long appointmentId
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.finishAppointment(landlordId, appointmentId);
        return Result.success();
    }

    @Operation(summary = "创建租约")
    @PostMapping("/lease/add")
    public Result<Void> createLease(HttpServletRequest req, @RequestBody @Valid LeaseCreateRequest request) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.createLease(landlordId, request);
        return Result.success();
    }

    @Operation(summary = "租约列表")
    @GetMapping("/lease/list")
    public Result<PageResult<LeaseContractVO>> listLeases(
            HttpServletRequest req,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        return Result.success(landlordService.listLeases(landlordId, status, pageNum, pageSize));
    }

    @Operation(summary = "创建租金账单")
    @PostMapping("/rent-bill/add")
    public Result<Void> createRentBill(HttpServletRequest req, @RequestBody @Valid RentBillCreateRequest request) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.createRentBill(landlordId, request);
        return Result.success();
    }

    @Operation(summary = "租金账单列表")
    @GetMapping("/rent-bill/list")
    public Result<PageResult<RentBillVO>> listRentBills(
            HttpServletRequest req,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum 不能小于 1") Long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize 不能小于 1") Long pageSize
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        return Result.success(landlordService.listRentBills(landlordId, status, pageNum, pageSize));
    }

    @Operation(summary = "标记账单已收款")
    @PostMapping("/rent-bill/receive/{billId}")
    public Result<Void> markBillReceived(
            HttpServletRequest req,
            @PathVariable("billId") @Min(value = 1, message = "billId 不能小于 1") Long billId,
            @RequestBody @Valid RentBillReceiveRequest request
    ) {
        Long landlordId = (Long) req.getAttribute("userId");
        landlordService.markBillReceived(landlordId, billId, request);
        return Result.success();
    }
}
