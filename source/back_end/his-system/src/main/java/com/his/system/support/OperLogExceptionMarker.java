package com.his.system.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 把异常挂到请求属性上，供 {@link OperLogInterceptor} 判定操作成败。
 *
 * <p>为什么需要它：业务异常被 {@code GlobalExceptionHandler} 兜成了 HTTP 200 + code=500，
 * Spring MVC 随后调 {@code afterCompletion} 传的 {@code ex} 是 <b>null</b> ——
 * 只看 {@code ex} 的话，"收费失败/审批被驳回"这类真实失败会全被记成"操作成功"，
 * 操作日志里那条 status 就永远等于 0，等于没记。
 *
 * <p>这里返回 {@code null} = 本 resolver 不处理，继续交给 {@code ExceptionHandlerExceptionResolver}，
 * 所以响应的格式与原来一字不差。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OperLogExceptionMarker implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (ex != null) {
            request.setAttribute(OperLogInterceptor.ATTR_EXCEPTION, ex);
        }
        return null;
    }
}
