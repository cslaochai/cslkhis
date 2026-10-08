package com.his.common.util;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 摘要与签名/验签的**唯一实现点**。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignCryptoUtil {

    public static final String DIGEST_ALGO = "SHA256";
    public static final String SIGN_ALGO = "SHA256withRSA";
    public static final String KEY_ALGO = "RSA";

    /**
     * SHA-256 摘要（十六进制小写）
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
     * 用私钥签名，返回 Base64 签名值
     */
    public static String sign(String privateKeyPem, String content) {
        try {
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initSign(PemCodecUtil.parsePrivateKey(privateKeyPem));
            sig.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new SignException("签名计算失败：" + e.getMessage(), e);
        }
    }

    /**
     * 用公钥验签。
     *
     * @return true=签名值与公钥匹配；false=不匹配（内容被改过，或签名值被换过）
     */
    public static boolean verify(String publicKeyPem, String content, String signBase64) {
        if (!TextUtil.hasText(signBase64)) {
            return false;
        }
        try {
            Signature sig = Signature.getInstance(SIGN_ALGO);
            sig.initVerify(PemCodecUtil.parsePublicKey(publicKeyPem));
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
     * 公钥指纹：对公钥 DER 做 SHA-256，用于人工核对"两处看到的是不是同一把公钥"
     */
    public static String fingerprint(String publicKeyPem) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] der = PemCodecUtil.parsePublicKey(publicKeyPem).getEncoded();
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
     * 签名层统一异常：任何"密钥/算法/编码坏了"的情况都走这里，不与业务异常混在一起
     */
    public static class SignException extends RuntimeException {
        public SignException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
