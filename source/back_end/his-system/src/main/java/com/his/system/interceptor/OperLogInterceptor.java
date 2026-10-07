package com.his.system.interceptor;

import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysOperLog;
import com.his.system.filter.OperLogCachingFilter;
import com.his.system.mapper.SysOperLogMapper;
import com.his.system.support.OperLogExceptionResolver;
import com.his.system.support.OperLogResultMarker;
import com.his.system.utils.RequestInfoUtils;
import com.his.system.utils.UserUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 操作日志拦截器（操作日志）。
 *
 * <p><b>口径：只记写动作。</b>POST/DELETE 里把约定命名的读接口（listPage / selectList /
 * getById / getDetailById …）挡掉，其余一律留痕。理由：读病历这类敏感查阅由业务模块自己写
 * 审计日志（有明确的"谁看了谁的病历"语义），操作日志若把每次翻页都记下来，
 * 真正的写操作会被淹没在噪音里，等保检查时反而拿不出东西。
 *
 * <p><b>旁路：</b>落库失败只打 error 日志，绝不让业务操作跟着失败；
 * 但也绝不静默吞 —— 出问题在日志里能查到。
 *
 * <p><b>登录不在这儿记：</b>/auth/login 等由 {@link com.his.system.service.SysLoginLogService} 单独记登录日志，两本账不重复。
 *
 * <p><b>失败判定：</b>业务异常被 {@code GlobalExceptionHandler} 兜成 HTTP 200 + code=500，
 * 拦截面上的 {@code ex} 会是 null —— 所以配了 {@link OperLogExceptionResolver} 把异常挂到请求属性上，
 * 这里读它才是准的（读不到再退回 HTTP 状态码 >= 400）。
 */
@Slf4j
@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1000)
@RequiredArgsConstructor
public class OperLogInterceptor implements HandlerInterceptor {

    /**
     * 待落库的日志骨架（preHandle 存、afterCompletion 用）
     */
    public static final String ATTR_PENDING = "his.operLog.pending";
    /**
     * 开始时间
     */
    public static final String ATTR_START = "his.operLog.start";
    /**
     * 异常标记（由 OperLogExceptionMarker 写入）
     */
    public static final String ATTR_EXCEPTION = "his.operLog.exception";

    /**
     * 读动作死表：URL 末段命中这些前缀/后缀的一律不记。
     * 命名来自 AGENTS.md 的接口约定（listPage / selectList / getById / getDetailById）。
     */
    private static final Set<String> READ_TAIL_EXACT = Set.of(
            "listPage", "selectList", "selectListPage", "getById", "getDetailById",
            "list", "tree", "options", "option", "summary", "stats", "stat", "overview",
            "count", "check", "match", "preview", "validate", "page", "scan",
            "login", "logout", "wxLogin", "sms", "sendSms", "captcha", "info", "postList", "roles");

    private static final String[] READ_PREFIX = {
            "list", "get", "select", "query", "search", "find", "count", "check",
            "match", "preview", "validate", "page", "tree", "option", "summary", "stat", "overview", "scan"};

    private static final String[] READ_SUFFIX = {
            "List", "Page", "Tree", "Options", "Summary", "Stat", "Stats", "Overview"};

    /**
     * 凭据类字段一律打码：日志里出现明文口令比没有日志更糟。
     */
    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "\"(password|oldPassword|newPassword|confirmPassword|payPassword|token|accessToken|refreshToken"
                    + "|secret|smsCode|captcha|verificationCode)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    private static final int PARAM_MAX = 2000;

    private final SysOperLogMapper sysOperLogMapper;

    private static String tail(String uri) {
        if (uri == null) {
            return "";
        }
        int i = uri.lastIndexOf('/');
        return i < 0 ? uri : uri.substring(i + 1);
    }

    /**
     * 读动作判定：末段整名命中死表，或以读前缀开头 / 读后缀结尾。
     */
    static boolean isReadAction(String tail) {
        if (tail == null || tail.isEmpty()) {
            return true;
        }
        String lower = tail.toLowerCase(Locale.ROOT);
        if (READ_TAIL_EXACT.contains(lower)) {
            return true;
        }
        for (String p : READ_PREFIX) {
            if (lower.startsWith(p)) {
                return true;
            }
        }
        for (String s : READ_SUFFIX) {
            if (tail.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    // 判定辅助

    /**
     * 业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）。
     * 顺序即优先级：deleteById 先判删除，techAuthUpsert 落到修改。
     */
    static int resolveBusinessType(String tail) {
        String k = tail.toLowerCase(Locale.ROOT);
        if (containsAny(k, "delete", "remove", "purge")) {
            return 3;
        }
        if (containsAny(k, "export", "download", "print")) {
            return 5;
        }
        if (containsAny(k, "import", "upload")) {
            return 6;
        }
        if (containsAny(k, "grant", "authorize", "rolemenu", "bindrole", "techauth", "assign")) {
            return 4;
        }
        if (containsAny(k, "clear", "reset", "truncate")) {
            return 7;
        }
        if (containsAny(k, "upsert", "update", "edit", "modify", "change", "cancel", "void", "revoke",
                "submit", "approve", "reject", "confirm", "execute", "settle", "charge", "refund",
                "switch", "bind", "unbind", "sign", "verify")) {
            return 2;
        }
        if (containsAny(k, "add", "create", "save", "insert", "register", "collect")) {
            return 1;
        }
        return 0;
    }

    private static boolean containsAny(String src, String... keys) {
        for (String k : keys) {
            if (src.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 操作模块：Controller 上的 @Tag（没有就退回类名去掉 Controller 后缀）。
     */
    private static String resolveTitle(Object handler) {
        if (handler instanceof HandlerMethod hm) {
            Tag tag = AnnotationUtils.findAnnotation(hm.getBeanType(), Tag.class);
            if (tag != null && TextUtil.hasText(tag.name())) {
                return TextUtil.cut(tag.name(), 100);
            }
            String simple = hm.getBeanType().getSimpleName();
            return TextUtil.cut(simple.endsWith("Controller") ? simple.substring(0, simple.length() - 10) : simple, 100);
        }
        return "系统";
    }

    private static String resolveMethodName(Object handler) {
        return handler instanceof HandlerMethod hm ? TextUtil.cut(hm.getMethod().getName(), 200) : null;
    }

    /**
     * 请求参数：queryString + 请求体（体由 OperLogCachingFilter 包装后缓存）。
     */
    private static String readParam(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        String qs = request.getQueryString();
        if (TextUtil.hasText(qs)) {
            sb.append(qs);
        }
        ContentCachingRequestWrapper wrapper = null;
        Object cached = request.getAttribute(OperLogCachingFilter.ATTR_CACHED);
        if (cached instanceof ContentCachingRequestWrapper w1) {
            wrapper = w1;
        } else if (request instanceof ContentCachingRequestWrapper w2) {
            wrapper = w2;
        }
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(new String(buf, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    static String mask(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        return SECRET_PATTERN.matcher(json).replaceAll("\"$1\":\"******\"");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return true;
        }
        String uri = request.getRequestURI();
        String tail = tail(uri);
        if (isReadAction(tail)) {
            return true;
        }
        try {
            SysOperLog row = new SysOperLog();
            row.setTitle(resolveTitle(handler));
            row.setBusinessType(resolveBusinessType(tail));
            row.setMethod(resolveMethodName(handler));
            row.setRequestMethod(method.toUpperCase(Locale.ROOT));
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null) {
                row.setOperId(user.getUserId());
                row.setOperName(user.getRealName());
                row.setDeptId(user.getDeptId());
                row.setDeptName(user.getDeptName());
            }
            row.setOperUrl(uri.length() > 500 ? uri.substring(0, 500) : uri);
            row.setOperIp(RequestInfoUtils.clientIp(request));
            row.setOperLocation(RequestInfoUtils.isPrivateIp(row.getOperIp()) ? "内网" : "外网");
            row.setOperTime(LocalDateTime.now());
            request.setAttribute(ATTR_PENDING, row);
            request.setAttribute(ATTR_START, System.currentTimeMillis());
        } catch (Exception e) {
            // 骨架拼不出来就放弃这一条，绝不影响业务
            log.warn("操作日志骨架构造失败 uri={}", uri, e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object pending = request.getAttribute(ATTR_PENDING);
        if (!(pending instanceof SysOperLog row)) {
            return;
        }
        Object startObj = request.getAttribute(ATTR_START);
        long start = startObj instanceof Long l ? l : System.currentTimeMillis();
        Object exObj = request.getAttribute(ATTR_EXCEPTION);
        Throwable real = exObj instanceof Throwable t ? t : ex;
        boolean businessFailed = Boolean.TRUE.equals(request.getAttribute(OperLogResultMarker.ATTR_FAILED));
        Object failMsg = request.getAttribute(OperLogResultMarker.ATTR_FAIL_MSG);
        try {
            row.setCostTime(System.currentTimeMillis() - start);
            boolean failed = real != null || businessFailed || response.getStatus() >= 400;
            row.setStatus(failed ? 1 : 0);
            row.setErrorMsg(real != null
                    ? TextUtil.cut(real.getClass().getSimpleName() + ": " + real.getMessage(), 1000)
                    : (failMsg == null ? null : TextUtil.cut(String.valueOf(failMsg), 1000)));
            row.setOperParam(TextUtil.cut(mask(readParam(request)), PARAM_MAX));
            row.setCreateBy(row.getOperName());
            sysOperLogMapper.insert(row);
        } catch (Exception e) {
            log.error("操作日志写入失败 uri={}", request.getRequestURI(), e);
        } finally {
            request.removeAttribute(ATTR_PENDING);
            request.removeAttribute(ATTR_START);
            request.removeAttribute(ATTR_EXCEPTION);
            request.removeAttribute(OperLogResultMarker.ATTR_FAILED);
            request.removeAttribute(OperLogResultMarker.ATTR_FAIL_MSG);
        }
    }
}
