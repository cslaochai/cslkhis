package com.his.support;

import com.his.common.base.Result;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务结果码指标：his_api_result_total{module,uri,code}。
 *
 * <p><b>为什么必须有它</b>：本项目的失败不走 HTTP 状态码 —— 业务异常经 GlobalExceptionHandler 兜成
 * {@code 200 + {code: 业务码}}（见 AGENTS §1）。只看 http.server.requests 的 status 标签，
 * 满屏都是 200，"业务失败率"恒等于 0，收费/挂号失败在监控上完全不可见。这里从统一响应体里取真实业务码。</p>
 *
 * <p>标签基数可控：uri 取路由模板（/patient/getById/{id}）而不是真实路径；未命中路由的一律记 UNKNOWN。</p>
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResultMetricsAdvice implements ResponseBodyAdvice<Object> {

    private static final String METRIC = "his.api.result";
    private static final String UNKNOWN_URI = "UNKNOWN";

    private final MeterRegistry registry;

    /**
     * 类名 → 模块名。按 Class 缓存，避免每个请求都做一次注解查找。
     */
    private final Map<Class<?>, String> moduleCache = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 只统计统一响应壳；文件流、字符串等非 Result 出参不计
        if (!(body instanceof Result<?> result)) {
            return body;
        }
        HttpServletRequest servletRequest = request instanceof ServletServerHttpRequest servlet
                ? servlet.getServletRequest() : null;
        Counter.builder(METRIC)
                .description("接口业务结果码（HTTP 200 也计数，业务失败体现在 code 上）")
                .tag("module", moduleOf(returnType, servletRequest))
                .tag("uri", uriPatternOf(servletRequest))
                .tag("code", String.valueOf(result.getCode()))
                .register(registry)
                .increment();
        return body;
    }

    /**
     * 路由模板：Spring 已把 /patient/getById/1857... 归一成 /patient/getById/{id}。
     * 拿不到就记 UNKNOWN —— 记原始路径会带来无限基数，几天把 Prometheus 内存撑满。
     */
    private static String uriPatternOf(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_URI;
        }
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern == null ? UNKNOWN_URI : pattern.toString();
    }

    /**
     * 模块名取 Controller 上的 @Tag（操作日志也是这个口径），没有就退回类名去 Controller 后缀。
     *
     * <p>异常路径的 {@code returnType} 是 GlobalExceptionHandler 的方法，直接取会把所有业务失败的
     * module 都记成「GlobalExceptionHandler」—— 先看请求上匹配到的真实 HandlerMethod，再退回来。</p>
     */
    private String moduleOf(MethodParameter returnType, HttpServletRequest request) {
        Class<?> controllerClass = returnType.getContainingClass();
        if (request != null) {
            Object matched = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (matched instanceof HandlerMethod handlerMethod) {
                controllerClass = handlerMethod.getBeanType();
            }
        }
        return moduleCache.computeIfAbsent(controllerClass, clazz -> {
            Tag tag = AnnotationUtils.findAnnotation(clazz, Tag.class);
            if (tag != null && !tag.name().isEmpty()) {
                return tag.name();
            }
            String simpleName = clazz.getSimpleName();
            return simpleName.endsWith("Controller")
                    ? simpleName.substring(0, simpleName.length() - "Controller".length())
                    : simpleName;
        });
    }
}
