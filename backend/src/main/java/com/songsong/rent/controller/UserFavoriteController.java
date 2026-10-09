package com.songsong.rent.controller;

import com.songsong.rent.common.PageResult;
import com.songsong.rent.common.Result;
import com.songsong.rent.service.UserFavoriteService;
import com.songsong.rent.vo.HouseListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户收藏")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @Operation(summary = "添加收藏")
    @PostMapping("/add/{houseId}")
    public Result<Void> addFavorite(HttpServletRequest req, @PathVariable("houseId") Long houseId) {
        Long userId = (Long) req.getAttribute("userId");
        userFavoriteService.addFavorite(userId, houseId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove/{houseId}")
    public Result<Void> removeFavorite(HttpServletRequest req, @PathVariable("houseId") Long houseId) {
        Long userId = (Long) req.getAttribute("userId");
        userFavoriteService.removeFavorite(userId, houseId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{houseId}")
    public Result<Boolean> checkFavorite(HttpServletRequest req, @PathVariable("houseId") Long houseId) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(userFavoriteService.checkFavorite(userId, houseId));
    }

    @Operation(summary = "我的收藏列表")
    @GetMapping("/my-list")
    public Result<PageResult<HouseListVO>> myList(
            HttpServletRequest req,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(userFavoriteService.listFavorites(userId, pageNum, pageSize));
    }
}