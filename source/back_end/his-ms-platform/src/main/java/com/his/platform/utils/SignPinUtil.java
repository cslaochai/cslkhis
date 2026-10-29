package com.his.platform.utils;

import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 签名 PIN 的哈希与校验（软签二次确认）。
 *
 * <p>与私钥保护（{@link KeyProtectorUtil}）是两回事：私钥用主口令加密保存（需可逆），
 * PIN 只做单向哈希（不可逆），明文 PIN 任何时候都不入库、不落日志、不出参。
 */
@Component
public class SignPinUtil {

    /**
     * PIN 哈希派生迭代次数（独立于私钥的迭代次数口径：PIN 位数少、熵低，需更多轮次抗爆破）
     */
    private static final int PIN_ITERATIONS = 200000;

    private static final int SALT_LENGTH = 16;

    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    /**
     * 新盐（Base64）
     */
    public String newSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 计算 PIN 哈希（Base64）。PIN 为空直接拒绝——不允许空 PIN 绕过二次确认。
     */
    public String hash(String pin, String saltBase64, int iterations) {
        if (!TextUtil.hasText(pin)) {
            throw new BusinessException("签名 PIN 不能为空：软签要求每次签名都由签名人本人输入 PIN 确认");
        }
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt,
                    iterations > 0 ? iterations : PIN_ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new SignCryptoUtil.SignException("PIN 哈希计算失败：" + e.getMessage(), e);
        }
    }

    /**
     * 校验 PIN。哈希不匹配即失败——不做「提示 PIN 错误」以外的任何泄露。
     */
    public boolean verify(String pin, String saltBase64, int iterations, String expectedHash) {
        if (!TextUtil.hasText(pin) || !TextUtil.hasText(expectedHash) || !TextUtil.hasText(saltBase64)) {
            return false;
        }
        try {
            String actual = hash(pin, saltBase64, iterations);
            return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8),
                    expectedHash.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 默认迭代次数（新增证书用）
     */
    public int defaultIterations() {
        return PIN_ITERATIONS;
    }
}
