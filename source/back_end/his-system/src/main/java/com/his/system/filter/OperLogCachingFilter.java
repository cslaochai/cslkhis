package com.his.system.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 为操作日志缓存请求体。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperLogCachingFilter extends OncePerRequestFilter {

    /**
     * 包装后的请求要挂到属性上：Spring Security 的 {@code SecurityContextHolderAwareRequestFilter}
     * 会在本过滤器之后再把请求包一层，拦截器拿到的不是我们包的那个对象，
     * 光靠 {@code instanceof ContentCachingRequestWrapper} 判断永远是 false（本轮实测：oper_param 恒为空）。
     */
    public static final String ATTR_CACHED = "his.operLog.cachedRequest";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String contentType = request.getContentType();
        boolean needCache = ("POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
                && contentType != null && contentType.toLowerCase().startsWith("application/json");
        if (needCache) {
            ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
            request.setAttribute(ATTR_CACHED, wrapper);
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
