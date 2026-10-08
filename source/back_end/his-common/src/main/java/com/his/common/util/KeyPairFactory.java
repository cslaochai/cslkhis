package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * RSA 密钥对生成。密钥长度固定 2048 —— 不再提供"可配置长度"，
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyPairFactory {

    public static final int KEY_SIZE = 2048;

    /**
     * 生成一对 RSA 密钥，返回 PEM 文本（公钥 X.509 / 私钥 PKCS#8）
     */
    public static KeyPairPem generate() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(SignCryptoUtil.KEY_ALGO);
            gen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair kp = gen.generateKeyPair();
            return new KeyPairPem(PemCodecUtil.toPem(kp.getPublic()), PemCodecUtil.toPem(kp.getPrivate()));
        } catch (Exception e) {
            throw new SignCryptoUtil.SignException("RSA 密钥对生成失败：" + e.getMessage(), e);
        }
    }

    public record KeyPairPem(String publicPem, String privatePem) {
    }
}
