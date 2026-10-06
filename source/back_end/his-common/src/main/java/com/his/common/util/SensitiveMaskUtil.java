package com.his.common.util;

import lombok.NoArgsConstructor;

/**
 * 敏感字段脱敏 —— 手机号 / 证件号 / 邮箱。
 *
 * <p>口径与前端 {@code src/lib/patientField.js} 的 maskMiddle 完全一致：保留前 head 后 tail，
 * 中间补 {@code *}，星数等于被遮位数（总长度不变），便于一眼看出是「同一串被打码」。
 *
 * <p>为什么放在后端：脱敏规则只在前端做等于没做 —— 明文仍在响应体里，
 * 抓包、浏览器插件、日志采集都能拿到；接口一旦复用到第二个页面，
 * 忘记调 mask 的那个页面就漏了。展示型接口出参时打码，前端只负责渲染。
 */

@NoArgsConstructor
public final class SensitiveMaskUtil {

    /**
     * 手机号：18878885878 → 188****5878；带区号固话保住区号 0731-****6666
     */
    public static String maskPhone(String v) {
        String s = trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int dash = s.indexOf('-');
        if (dash > 0) {
            return s.substring(0, dash + 1) + maskMiddle(s.substring(dash + 1), 0, 4);
        }
        return maskMiddle(s, 3, 4);
    }

    /**
     * 身份证号：430726199709180511 → 4307**********0511（前 4 是省市，后 4 足以核对是否同一张证）
     */
    public static String maskIdCard(String v) {
        String s = trimToEmpty(v);
        return s.isEmpty() ? null : maskMiddle(s, 4, 4);
    }

    /**
     * 邮箱：1688888@qq.com → 168****8@qq.com（域名保留，用户名打码）
     */
    public static String maskEmail(String v) {
        String s = trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int at = s.indexOf('@');
        if (at <= 0) {
            return maskMiddle(s, 1, 1);
        }
        return maskMiddle(s.substring(0, at), 3, 0) + s.substring(at);
    }

    /**
     * 卡号（医保卡号 / 就诊卡号）：固定 4 星而不是等长星号，长度本身不泄露。
     * <p>短号只留首尾各 2 位，7 位以下全遮 —— 露出过半就不算脱敏了。
     */
    public static String maskCardNo(String v) {
        String s = trimToEmpty(v);
        if (s.isEmpty()) {
            return null;
        }
        int len = s.length();
        if (len >= 12) {
            return s.substring(0, 4) + "****" + s.substring(len - 4);
        }
        if (len >= 8) {
            return s.substring(0, 2) + "****" + s.substring(len - 2);
        }
        return "****";
    }

    /**
     * 中间打码：保留前 head 位 + 后 tail 位，中间补 {@code *}。
     *
     * <p>长度不足 head+tail 时退化为只留首尾各一位，避免出现「全露出」或「负数星号」。
     */
    public static String maskMiddle(String v, int head, int tail) {
        String s = trimToEmpty(v);
        if (s.isEmpty()) {
            return "";
        }
        int len = s.length();
        if (len <= 2) {
            return repeat('*', len);
        }
        if (len <= head + tail) {
            return s.substring(0, 1) + repeat('*', len - 2) + s.substring(len - 1);
        }
        return s.substring(0, head) + repeat('*', len - head - tail) + s.substring(len - tail);
    }

    private static String repeat(char c, int n) {
        return n <= 0 ? "" : String.valueOf(c).repeat(n);
    }

    private static String trimToEmpty(String v) {
        return v == null ? "" : v.trim();
    }
}
