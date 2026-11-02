package com.his.platform.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * CA 推送报文验签工具：SHA-256 摘要与 HMAC-SHA256，统一输出小写 hex。
 *
 * <p>与 CA 侧（csssl）的签名实现同口径，签名串为：
 * {@code timestamp + "\n" + nonce + "\n" + sha256Hex(原始请求体字节)}。
 *
 * <p>注意哈希必须算<b>原始请求体字节</b>：先反序列化成对象再重新序列化，
 * 字段顺序或空格一变签名就对不上。
 */
public final class PushSignUtil {

    private PushSignUtil() {
    }

    /**
     * SHA-256 摘要，返回小写 hex（64 字符）
     */
    public static String sha256Hex(byte[] data) {
        try {
            return hex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /**
     * HMAC-SHA256，返回小写 hex（64 字符）
     */
    public static String hmacHex(String secret, String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return hex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }

    /**
     * 常量时间比较，避免逐字节比对泄露时序信息
     */
    public static boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    private static String hex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        char[] digits = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = digits[(bytes[i] >> 4) & 0x0F];
            out[i * 2 + 1] = digits[bytes[i] & 0x0F];
        }
        return new String(out);
    }
}
