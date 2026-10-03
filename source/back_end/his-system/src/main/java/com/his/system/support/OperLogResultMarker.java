package com.his.system.support;

import com.his.common.base.Result;
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
 * 把"业务失败"标记到请求属性上，供 {@link OperLogInterceptor} 判定操作成败。
 *
 * <p>为什么有了 {@link OperLogExceptionMarker} 还要它：大量业务失败是
 * <b>不抛异常</b>的 —— 例如改密码时旧密码填错，Controller 直接 return Result.error("旧密码错误")，
 * HTTP 200 + code=500。只看异常的话，这类真实失败会全被记成"操作成功"，
 * 操作日志里的 status 就永远等于 0，等于没记（等保要看的正是"事件是否成功"）。
 *
 * <p>这里只做标记，原样返回 body，响应的字节一个都不动。
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
