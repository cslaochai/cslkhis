package com.his.system.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 从 HTTP 请求里解析 IP / 浏览器 / 操作系统等环境信息。
 *
 * <p><b>登录日志与操作日志共用这一套口径</b>：两边都从这里取 IP、判内外网。
 * 否则同一台机器在两本账里一个记"内网"一个记"外网"，事后对账根本对不上。
 *
 * <p>纯静态解析，不落库、不依赖 Spring 容器 —— 可以被任意拦截器/服务直接调。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestInfoUtils {

    /**
     * 取客户端 IP 时依次尝试的代理头（反向代理后 RemoteAddr 只剩网关地址）。
     */
    private static final String[] IP_HEADERS = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};

    /**
     * 客户端 IP：先按代理头拿，拿不到再用 {@code RemoteAddr}。
     *
     * @param request 当前请求，允许为 null（定时任务/内部调用没有请求上下文）
     * @return IP；无请求时返回 null，不猜
     */
    public static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String h : IP_HEADERS) {
            String v = request.getHeader(h);
            if (StringUtils.hasText(v) && !"unknown".equalsIgnoreCase(v)) {
                return v.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 内网网段判定（10/8、172.16/12、192.168/16、127/8、::1）。不认识的公网 IP 一律"外网"，不猜城市。
     */
    public static boolean isPrivateIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        String v = ip.trim();
        if ("::1".equals(v) || "0:0:0:0:0:0:0:1".equals(v) || v.startsWith("127.")) {
            return true;
        }
        if (v.startsWith("10.") || v.startsWith("192.168.")) {
            return true;
        }
        if (v.startsWith("172.")) {
            String[] parts = v.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            int n = Integer.parseInt(parts[1]);
            return n >= 16 && n <= 31;
        }
        return false;
    }

    /**
     * 浏览器识别：只认主流内核，认不出就留空（不猜）。
     */
    public static String parseBrowser(String ua) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        String u = ua.toLowerCase();
        if (u.contains("edg/")) {
            return "Edge";
        }
        if (u.contains("chrome/") && !u.contains("chromium")) {
            return "Chrome";
        }
        if (u.contains("firefox/")) {
            return "Firefox";
        }
        if (u.contains("safari/") && !u.contains("chrome")) {
            return "Safari";
        }
        if (u.contains("postmanruntime") || u.contains("okhttp") || u.contains("node")) {
            return "接口客户端";
        }
        return null;
    }

    /**
     * 操作系统识别：只认主流系统，认不出就留空（不猜）。
     */
    public static String parseOs(String ua) {
        if (!StringUtils.hasText(ua)) {
            return null;
        }
        String u = ua.toLowerCase();
        if (u.contains("android")) {
            return "Android";
        }
        if (u.contains("iphone") || u.contains("ipad") || u.contains("ios")) {
            return "iOS";
        }
        if (u.contains("windows")) {
            return "Windows";
        }
        if (u.contains("mac os x") || u.contains("macintosh")) {
            return "MacOS";
        }
        if (u.contains("linux")) {
            return "Linux";
        }
        return null;
    }
}
