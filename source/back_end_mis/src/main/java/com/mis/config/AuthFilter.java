package com.mis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mis.common.base.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器（轻量版，不引入 Spring Security）。
 * <p>白名单直接放行；其余请求必须携带合法 Bearer token，否则统一 401 JSON。
 * token 只做身份解析（userId/username/roleCode/dept），权限码与菜单由
 * /auth/info、/auth/menus 按需查询。
 * <p>机构 2304/2305 报盘与参保查询属 HIS 侧联调接口，不走登录态，放行。
 */
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/gateway/",
            "/insure/person/query",
            "/doc.html",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs/",
            "/webjars/",
            "/favicon.ico",
            "/error",
    };

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (isWhiteListed(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        if (token != null && jwtUtils.validateToken(token)) {
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(jwtUtils.getUserIdFromToken(token));
            loginUser.setUsername(jwtUtils.getUsernameFromToken(token));
            loginUser.setRoleCode(jwtUtils.getRoleCodeFromToken(token));
            loginUser.setDeptId(jwtUtils.getDeptIdFromToken(token));
            loginUser.setDeptName(jwtUtils.getDeptNameFromToken(token));
            UserContext.set(loginUser);
            try {
                filterChain.doFilter(request, response);
            } finally {
                UserContext.clear();
            }
            return;
        }

        writeUnauthorized(response);
    }

    private boolean isWhiteListed(String path) {
        for (String prefix : WHITE_LIST) {
            if (path.equals(prefix) || (prefix.endsWith("/") && path.startsWith(prefix))) {
                return true;
            }
        }
        return false;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或登录已过期")));
    }
}
