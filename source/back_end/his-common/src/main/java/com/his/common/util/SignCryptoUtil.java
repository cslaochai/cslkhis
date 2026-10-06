package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 摘要与签名/验签的**唯一实现点**。
 *
 * <p>三个铁律：
 * <ol>
 *   <li>摘要算法固定 SHA-256（十六进制小写，64 字符），签名算法固定 SHA256withRSA。
 *       摘要值会被写进库、被印在页面上、被人工比对 —— 大小写不统一会让"看着一样其实不一样"。</li>
 *   <li>被签内容一律按 UTF-8 编码。**不允许调用方传字节数组**，
 *       避免"一边按 UTF-8、一边按平台默认编码"这种只在中文内容上暴露的坑。</li>
 *   <li>验签失败（{@code false}）与验签异常（抛错）都必须能被区分：
 *       前者是"证据不对"，后者是"密钥/算法坏了"。本类把异常一律收敛成
 *       {@link SignException} 抛出，绝不 swallow 成 {@code false} ——
 *       把"密钥解析不了"报成"签名不匹配"会让人去查错方向。</li>
 * </ol>
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
        if (signBase64 == null || signBase64.isBlank()) {
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
