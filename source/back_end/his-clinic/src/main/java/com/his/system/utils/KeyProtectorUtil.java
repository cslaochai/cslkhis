package com.his.system.utils;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.config.SignProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 私钥静态保护：PBKDF2-HMAC-SHA256 派生 + AES-256-GCM 加密。
 */
@Component
public class KeyProtectorUtil {

    /**
     * GCM 标准 IV 长度
     */
    private static final int IV_LENGTH = 12;
    /**
     * GCM 认证标签长度（位）
     */
    private static final int TAG_BITS = 128;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_BITS = 256;
    private final SecureRandom random = new SecureRandom();

    private final SignProperties properties;

    public KeyProtectorUtil(SignProperties properties) {
        this.properties = properties;
    }

    /**
     * 主口令缺失时不允许签发 —— 由调用方在签发入口先调，报错信息直指环境变量名
     */
    public void requireSecret() {
        if (!TextUtil.hasText(properties.getMasterSecret())) {
            throw new BusinessException("未配置签名主口令（环境变量 HIS_SIGN_SECRET），无法安全托管私钥；为避免私钥明文落库，已拒绝签发证书");
        }
    }

    public String newSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 加密私钥 PEM，返回 Base64(iv||cipher)
     */
    public String protect(String privatePem, String saltBase64, int iterations) {
        requireSecret();
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(saltBase64, iterations),
                    new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(privatePem.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new SignCryptoUtil.SignException("私钥加密失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解密私钥 PEM。口令不对或密文被改 → GCM 校验失败抛异常（不会被静默吞掉）
     */
    public String unprotect(String protectedBase64, String saltBase64, int iterations) {
        requireSecret();
        try {
            byte[] all = Base64.getDecoder().decode(protectedBase64);
            if (all.length <= IV_LENGTH) {
                throw new IllegalArgumentException("私钥密文长度不合法");
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(all, 0, iv, 0, IV_LENGTH);
            byte[] ct = new byte[all.length - IV_LENGTH];
            System.arraycopy(all, IV_LENGTH, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(saltBase64, iterations),
                    new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("私钥解密失败（主口令不符或密文被篡改），该证书已不可用于签名；请吊销后重新签发。原因：" + e.getMessage());
        }
    }

    private SecretKeySpec deriveKey(String saltBase64, int iterations) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        PBEKeySpec spec = new PBEKeySpec(properties.effectiveSecret().toCharArray(),
                salt, iterations, KEY_BITS);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}
