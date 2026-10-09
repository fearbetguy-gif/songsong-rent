package com.songsong.rent.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.songsong.rent.common.ResultCode;
import com.songsong.rent.entity.User;
import com.songsong.rent.exception.BusinessException;
import com.songsong.rent.mapper.UserMapper;
import com.songsong.rent.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserAuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        String token = authHeader.substring(7);
        Long userId;
        try {
            DecodedJWT jwt = JwtUtil.verifyToken(token);
            userId = jwt.getClaim("userId").asLong();
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }

        request.setAttribute("userId", userId);
        return true;
    }
}