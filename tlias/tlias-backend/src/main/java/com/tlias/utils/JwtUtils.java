package com.tlias.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>密钥、有效期均在 application.yml 的 {@code tlias.jwt.*} 中配置。</p>
 */
@Component
public class JwtUtils {

    @Value("${tlias.jwt.secret:tlias-default-secret-key-please-change-it-0123456789}")
    private String secret;

    /** 令牌有效期，单位毫秒，默认 24 小时 */
    @Value("${tlias.jwt.expire:86400000}")
    private long expire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 令牌
     */
    public String createToken(Integer id, String username) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put("id", id);
        claims.put("username", username);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(id))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT 令牌，解析失败（篡改 / 过期 / 格式错误）返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
