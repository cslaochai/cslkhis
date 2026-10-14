package com.mis.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类（照搬 back_end JwtUtils 口径，去掉多角色/切科室逻辑）
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token：携带当前角色与当前科室（随会话走，读取侧不再查库）。
     */
    public String generateToken(Long userId, String username, String roleCode, Long deptId, String deptName) {
        Map<String, Object> claims = new HashMap<>();
        // 大整数以字符串进 claims，避免 jjwt 解析时经 Double 丢精度
        claims.put("userId", userId == null ? null : String.valueOf(userId));
        claims.put("username", username);
        claims.put("roleCode", roleCode);
        claims.put("deptId", deptId == null ? null : String.valueOf(deptId));
        claims.put("deptName", deptName);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return toLong(getClaimsFromToken(token).get("userId"));
    }

    public String getRoleCodeFromToken(String token) {
        return getClaimsFromToken(token).get("roleCode", String.class);
    }

    public Long getDeptIdFromToken(String token) {
        return toLong(getClaimsFromToken(token).get("deptId"));
    }

    public String getDeptNameFromToken(String token) {
        Object value = getClaimsFromToken(token).get("deptName");
        return value == null ? null : String.valueOf(value);
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
