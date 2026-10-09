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
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if ("/user/login".equals(uri) || "/user/register".equals(uri) || "/user/admin-login".equals(uri)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && ("/house/list".equals(uri) || "/house/stats".equals(uri) || uri.matches("^/house/\\d+$"))) {
            return true;
        }

        // 需要拦截的写操作：POST, PUT, DELETE
        // 对预约相关的接口进行豁免，让它们在各自的 Controller/Service 中自行校验用户
        if (uri.startsWith("/appointment/")) {
            return true;
        }
        if (uri.startsWith("/landlord/")) {
            return true;
        }
        if (uri.startsWith("/consult/")) {
            return true;
        }
        
        // 特殊处理 /appointment-admin/**，只允许管理员访问
        if (uri.startsWith("/appointment-admin/")) {
            return checkAdminToken(request);
        }

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            // 除了放行的写接口，其他都需要管理员权限
            if (uri.startsWith("/user/login") || uri.startsWith("/user/register") || uri.startsWith("/upload/")) {
                return true;
            }
            return checkAdminToken(request);
        }
        return true;
    }

    private boolean checkAdminToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "未登录或Token格式不正确");
        }

        String token = authHeader.substring(7);
        Long userId;
        Integer isAdmin;
        try {
            DecodedJWT jwt = JwtUtil.verifyToken(token);
            userId = jwt.getClaim("userId").asLong();
            isAdmin = jwt.getClaim("isAdmin").asInt();
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Token无效或已过期，请重新登录");
        }

        if (isAdmin == null || isAdmin != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号无后台管理权限");
        }

        // 进一步查询数据库以校验确保用户未被物理删除或降级
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsAdmin() == null || user.getIsAdmin() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号已被禁用或取消管理员权限");
        }

        // 将 userId 存入 request，方便后续 Controller 使用
        request.setAttribute("adminUserId", userId);
        return true;
    }
}
