package com.his.system.support;

import com.his.system.interceptor.OperLogInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 把异常挂到请求属性上，供 OperLogInterceptor 判定操作成败。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperLogExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (ex != null) {
            request.setAttribute(OperLogInterceptor.ATTR_EXCEPTION, ex);
        }
        return null;
    }
}
