package com.songsong.rent.controller;

import com.songsong.rent.common.Result;
import com.songsong.rent.dto.ConsultMessageSendRequest;
import com.songsong.rent.dto.HouseAppointmentRejectRequest;
import com.songsong.rent.service.ConsultService;
import com.songsong.rent.vo.ConsultMessageVO;
import com.songsong.rent.vo.ConsultSessionVO;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "在线咨询")
@Validated
@RestController
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    @Operation(summary = "租客发送咨询消息给房东")
    @PostMapping("/send")
    public Result<Void> send(HttpServletRequest req, @RequestBody @Valid ConsultMessageSendRequest request) {
        Long userId = (Long) req.getAttribute("userId");
        consultService.sendToLandlord(userId, request);
        return Result.success();
    }

    @Operation(summary = "按房源查看咨询消息（当前登录用户可见）")
    @GetMapping("/house/{houseId}/messages")
    public Result<List<ConsultMessageVO>> listHouseMessages(
            HttpServletRequest req,
            @PathVariable("houseId") @Min(value = 1, message = "houseId 不能小于 1") Long houseId
    ) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(consultService.listHouseMessages(userId, houseId));
    }

    @Operation(summary = "房东会话列表")
    @GetMapping("/landlord/sessions")
    public Result<List<ConsultSessionVO>> listLandlordSessions(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(consultService.listLandlordSessions(userId));
    }

    @Operation(summary = "房东查看指定会话消息")
    @GetMapping("/landlord/messages")
    public Result<List<ConsultMessageVO>> listLandlordMessages(
            HttpServletRequest req,
            @RequestParam @Min(value = 1, message = "houseId 不能小于 1") Long houseId,
            @RequestParam @Min(value = 1, message = "tenantId 不能小于 1") Long tenantId
    ) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(consultService.listLandlordMessages(userId, houseId, tenantId));
    }

    @Operation(summary = "房东回复租客消息")
    @PostMapping("/landlord/reply")
    public Result<Void> landlordReply(
            HttpServletRequest req,
            @RequestParam @Min(value = 1, message = "houseId 不能小于 1") Long houseId,
            @RequestParam @Min(value = 1, message = "tenantId 不能小于 1") Long tenantId,
            @RequestBody @Valid HouseAppointmentRejectRequest request
    ) {
        Long userId = (Long) req.getAttribute("userId");
        consultService.landlordReply(userId, houseId, tenantId, request.getReason());
        return Result.success();
    }
}
