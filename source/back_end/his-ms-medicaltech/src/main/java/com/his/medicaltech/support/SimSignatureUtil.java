package com.his.medicaltech.support;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 仿真平台互联签名工具（HMAC-SHA256）。
 *
 * <p>签名串 = {@code timestamp + "\n" + nonce + "\n" + sha256Hex(原始请求体)}，
 * 密钥取 {@code his.simlab.secret}。两端同一套口径：HIS 推 simlab、simlab 回调 HIS
 * 都带 {@code X-Sim-Timestamp / X-Sim-Nonce / X-Sim-Signature} 三个头。
 *
 * <p>仅用于院内仿真环境互联；公网渠道通知仍走各自的验签过滤器，不走这里。
 */
public final class SimSignatureUtil {

    public static final String HEADER_TIMESTAMP = "X-Sim-Timestamp";
    public static final String HEADER_NONCE = "X-Sim-Nonce";
    public static final String HEADER_SIGNATURE = "X-Sim-Signature";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SimSignatureUtil() {
    }

    /** 生成随机 nonce（16 字节十六进制）。 */
    public static String newNonce() {
        byte[] buf = new byte[16];
        RANDOM.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    public static String sign(String secret, String timestamp, String nonce, byte[] body) {
        String canonical = timestamp + "\n" + nonce + "\n" + sha256Hex(body);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("仿真互联签名计算失败", e);
        }
    }

    /** 常量时间比对，防计时侧信道。 */
    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }
}
