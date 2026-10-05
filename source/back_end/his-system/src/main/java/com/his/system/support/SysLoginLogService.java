package com.his.system.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.SysLoginLog;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysLoginLogMapper;
import com.his.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录日志落库（登录日志）。
 *
 * <p><b>旁路写入：</b>登录是本系统的第一入口，日志写失败绝不能反过来把人挡在门外 ——
 * 所以整段 try/catch，失败只打 error 日志（与 {@code SysAuditLogService} 同一口径）。
 *
 * <p><b>成功失败都记</b>：只记成功的话，口令爆破在系统里是完全隐形的，
 * 等保三级「审计覆盖到每个用户、记录重要安全事件」这一条就空了。
 * 失败时 userId/realName 能查到就带上（用户名输错时只有 userName 有值）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysUserMapper userMapper;

    static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};
        for (String h : headers) {
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
    static boolean isPrivateIp(String ip) {
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
    static String parseBrowser(String ua) {
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

    static String parseOs(String ua) {
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

    private static String cut(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * 登录成功/失败/登出统一入口。
     */
    public void record(String userName, HttpServletRequest request, boolean success, String msg) {
        Long userId = null;
        String realName = null;
        if (StringUtils.hasText(userName)) {
            SysUser user = findUser(userName);
            if (user != null) {
                userId = user.getId();
                realName = user.getRealName();
            }
        }
        record(userName, userId, realName, request, success, msg);
    }

    /**
     * 已知用户身份时（登录成功链路，省一次回查）。
     */
    public void record(String userName, Long userId, String realName, HttpServletRequest request,
                       boolean success, String msg) {
        try {
            String ip = clientIp(request);
            String ua = request == null ? null : request.getHeader("User-Agent");
            SysLoginLog row = new SysLoginLog();
            row.setUserName(cut(userName, 64));
            row.setUserId(userId);
            row.setRealName(cut(realName, 64));
            row.setLoginIp(cut(ip, 50));
            row.setLoginLocation(ip == null ? null : (isPrivateIp(ip) ? "内网" : "外网"));
            row.setBrowser(cut(parseBrowser(ua), 100));
            row.setOs(cut(parseOs(ua), 100));
            row.setUserAgent(cut(ua, 500));
            row.setLoginStatus(success ? 0 : 1);
            row.setMsg(cut(msg, 200));
            row.setLoginTime(LocalDateTime.now());
            row.setCreateBy(cut(userName, 64));
            loginLogMapper.insert(row);
        } catch (Exception e) {
            log.error("登录日志写入失败 userName={} success={}", userName, success, e);
        }
    }

    private SysUser findUser(String userName) {
        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUserName, userName).last("LIMIT 1");
            return userMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("登录日志回填用户信息失败 userName={}", userName, e);
            return null;
        }
    }
}
