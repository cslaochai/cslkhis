package com.his.security;

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
 * JWT工具类
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
     * 生成JWT Token。
     *
     * <p><b>所有签发点必须走这一个方法</b>（登录 / 切角色 / 切科室）。原因是「当前角色」和
     * 「当前科室」都是**会话级选择**，它们只活在这个 token 里：
     * {@code UserDetailsServiceImpl} 每次都从库里重建 CurrentUser（科室取的是员工的**主科室**），
     * 所以只要重签 token 时漏带其中一个，用户切过的那一项就会
     * 在下一次请求里被库里的默认值悄悄覆盖 —— 表现就是「切换了啥都没变」，而且刷新页面
     * 也救不回来（前端 localStorage 说 A、后端按 B 算，两边各说各话）。
     *
     * <p>为什么不把 deptId 也做成「从库里查」：一个员工可以有多个科室
     * （员工岗位），库里记的那一份只是主科室，用户当场选了哪个只存在于会话中。
     *
     * @param currentRole 当前岗位角色编码，交给 {@code JwtAuthenticationFilter} 收敛权限用
     * @param deptId      当前科室ID（员工主科室或用户切换后的科室）
     * @param deptName    当前科室名称，随 token 一起带走，避免读侧再查一次库
     */
    public String generateToken(Long userId, String username, String currentRole,
                                Long deptId, String deptName) {
        Map<String, Object> claims = new HashMap<>();
        // 大整数 ID 必须以字符串进 claims：jjwt 解析 JSON 数字时经 Double 会丢精度
        // （90000000000009099 → 9000000000000009099），toLong 已兼容字符串入参
        claims.put("userId", userId == null ? null : String.valueOf(userId));
        claims.put("username", username);
        claims.put("currentRole", currentRole);
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

    /**
     * 从Token中获取当前角色
     */
    public String getCurrentRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("currentRole", String.class);
    }

    /**
     * 从Token中获取当前科室ID；token 里没有（老 token / 未切换过）时返回 null，
     * 由调用方决定是否退回库里的主科室。
     *
     * <p>按 Number 读而不是 {@code claims.get("deptId", Long.class)}：JSON 反序列化后
     * 小整数可能是 Integer，直接按 Long 取会抛类型不符。
     */
    public Long getDeptIdFromToken(String token) {
        return toLong(getClaimsFromToken(token).get("deptId"));
    }

    /**
     * 从Token中获取当前科室名称
     */
    public String getDeptNameFromToken(String token) {
        Object value = getClaimsFromToken(token).get("deptName");
        return value == null ? null : String.valueOf(value);
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

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return toLong(getClaimsFromToken(token).get("userId"));
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
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
