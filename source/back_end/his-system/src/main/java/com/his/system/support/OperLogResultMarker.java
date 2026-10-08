package com.his.system.support;

import com.his.common.base.Result;
import com.his.system.interceptor.OperLogInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 把"业务失败"标记到请求属性上，供 OperLogInterceptor 判定操作成败。
 */
@RestControllerAdvice
public class OperLogResultMarker implements ResponseBodyAdvice<Object> {

    public static final String ATTR_FAILED = "his.operLog.failed";
    public static final String ATTR_FAIL_MSG = "his.operLog.failMsg";

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> result && result.getCode() != 200) {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                HttpServletRequest req = attrs.getRequest();
                req.setAttribute(ATTR_FAILED, Boolean.TRUE);
                req.setAttribute(ATTR_FAIL_MSG, result.getMessage());
            }
        }
        return body;
    }
}
