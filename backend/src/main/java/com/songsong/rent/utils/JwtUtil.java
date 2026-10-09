package com.songsong.rent.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 86400000L * 7; // 7天有效期

    private static String getSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("请通过 JWT_SECRET 环境变量配置 JWT 密钥");
        }
        return secret;
    }

    public static String createToken(Long userId, Integer isAdmin) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("isAdmin", isAdmin)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(getSecret()));
    }

    public static DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(getSecret()))
                .build()
                .verify(token);
    }
}
