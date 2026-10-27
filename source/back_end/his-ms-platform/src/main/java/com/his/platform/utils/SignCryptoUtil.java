package com.his.platform.utils;

import com.his.common.util.TextUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 摘要与签名/验签的**唯一实现点**。
 *
 * <p>算法随密钥走：RSA 密钥用 SHA256withRSA / SHA-256；SM2 密钥用 SM3withSM2 / SM3
 * （国密，对齐 WS/T 847—2024 医学电子文档签名要求）。签名/验签/指纹按密钥算法自动路由，
 * 历史 RSA 证书不受影响（验签仍走 RSA 通道）。
 */
public final class SignCryptoUtil {

    public static final String DIGEST_ALGO = "SHA256";

    public static final String SIGN_ALGO = "SHA256withRSA";

    public static final String KEY_ALGO = "RSA";

    /** SM2 签名算法（SM3withSM2，BC provider） */
    public static final String SM2_SIGN_ALGO = "SM3withSM2";

    /** SM2 摘要算法 */
    public static final String SM2_DIGEST_ALGO = "SM3";

    /**
     * SHA-256 摘要（十六进制小写）。签名记录的内容摘要统一用 SHA-256，
     * 与证书算法解耦（SM3withSM2 内部的 SM3 已满足国密签名摘要要求）。
     */
    public static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new SignException("摘要计算失败：" + e.getMessage(), e);
        }
    }

    /**
     * 用私钥签名，返回 Base64 签名值。签名算法按私钥算法自动选择（SM2→SM3withSM2，RSA→SHA256withRSA）。
     */
    public static String sign(String privateKeyPem, String content) {
        try {
            java.security.PrivateKey key = PemCodecUtil.parsePrivateKey(privateKeyPem);
            Signature sig = Signature.getInstance(signAlgoForKey(key), "BC");
            sig.initSign(key);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new SignException("签名计算失败：" + e.getMessage(), e);
        }
    }

    /**
     * 用公钥验签。验签算法按公钥算法自动选择（SM2→SM3withSM2，RSA→SHA256withRSA）。
     *
     * @return true=签名值与公钥匹配；false=不匹配（内容被改过，或签名值被换过）
     */
    public static boolean verify(String publicKeyPem, String content, String signBase64) {
        if (!TextUtil.hasText(signBase64)) {
            return false;
        }
        try {
            java.security.PublicKey key = PemCodecUtil.parsePublicKey(publicKeyPem);
            Signature sig = Signature.getInstance(signAlgoForKey(key), "BC");
            sig.initVerify(key);
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signBase64));
        } catch (IllegalArgumentException e) {
            // 签名值不是合法 Base64：这是"内容不对"，不是"环境坏了"
            return false;
        } catch (Exception e) {
            throw new SignException("验签执行失败：" + e.getMessage(), e);
        }
    }

    /**
     * 公钥指纹：对公钥 DER 做摘要（RSA→SHA-256，SM2→SM3），用于人工核对"两处看到的是不是同一把公钥"。
     * 两种摘要均输出 32 字节，指纹定长 64 位十六进制。
     */
    public static String fingerprint(String publicKeyPem) {
        try {
            java.security.PublicKey key = PemCodecUtil.parsePublicKey(publicKeyPem);
            MessageDigest md = MessageDigest.getInstance(digestAlgoForKey(key), "BC");
            byte[] der = key.getEncoded();
            return HexFormat.of().formatHex(md.digest(der));
        } catch (Exception e) {
            throw new SignException("公钥指纹计算失败：" + e.getMessage(), e);
        }
    }

    /**
     * 指纹的分组展示（每 4 位一组，便于电话核对）
     */
    public static String fingerprintGroups(String fingerprint) {
        if (fingerprint == null || fingerprint.length() < 8) {
            return fingerprint;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fingerprint.length(); i += 4) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(fingerprint, i, Math.min(i + 4, fingerprint.length()));
        }
        return sb.toString();
    }

    /**
     * 证书算法标识（RSA2048 / SM2）→ 签名算法
     */
    public static String signAlgoFor(String keyAlgorithm) {
        return isSm2(keyAlgorithm) ? SM2_SIGN_ALGO : SIGN_ALGO;
    }

    /**
     * 证书算法标识（RSA2048 / SM2）→ 摘要算法
     */
    public static String digestAlgoFor(String keyAlgorithm) {
        return isSm2(keyAlgorithm) ? SM2_DIGEST_ALGO : DIGEST_ALGO;
    }

    /** 私钥对象 → 签名算法（EC/SM2 用 SM3withSM2，其余用 SHA256withRSA） */
    private static String signAlgoForKey(java.security.Key key) {
        return key != null && "EC".equals(key.getAlgorithm()) ? SM2_SIGN_ALGO : SIGN_ALGO;
    }

    /** 公钥对象 → 摘要算法（EC/SM2 用 SM3，其余用 SHA-256） */
    private static String digestAlgoForKey(java.security.Key key) {
        return key != null && "EC".equals(key.getAlgorithm()) ? SM2_DIGEST_ALGO : DIGEST_ALGO;
    }

    private static boolean isSm2(String keyAlgorithm) {
        return keyAlgorithm != null && ("SM2".equalsIgnoreCase(keyAlgorithm.trim())
                || "EC".equalsIgnoreCase(keyAlgorithm.trim()));
    }

    /**
     * 签名层统一异常：任何"密钥/算法/编码坏了"的情况都走这里，不与业务异常混在一起
     */
    public static class SignException extends RuntimeException {
        public SignException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
