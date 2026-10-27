package com.his.platform.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * 签名密钥对生成：RSA2048（默认）或 SM2（sm2p256v1 国密推荐曲线，BC provider）。
 */
public final class KeyPairFactory {

    public static final int KEY_SIZE = 2048;

    public static final String ALG_RSA2048 = "RSA2048";

    public static final String ALG_SM2 = "SM2";

    private KeyPairFactory() {
    }

    /**
     * 按证书算法生成密钥对（RSA2048 / SM2），返回 PEM 文本（公钥 X.509 / 私钥 PKCS#8）。
     */
    public static KeyPairPem generate(String keyAlgorithm) {
        if (ALG_SM2.equalsIgnoreCase(keyAlgorithm)) {
            return generateSm2();
        }
        return generateRsa2048();
    }

    /**
     * 生成一对 RSA 密钥，返回 PEM 文本（公钥 X.509 / 私钥 PKCS#8）
     */
    public static KeyPairPem generate() {
        return generateRsa2048();
    }

    private static KeyPairPem generateRsa2048() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(KEY_SIZE, new SecureRandom());
            KeyPair kp = gen.generateKeyPair();
            return new KeyPairPem(PemCodecUtil.toPem(kp.getPublic()), PemCodecUtil.toPem(kp.getPrivate()));
        } catch (Exception e) {
            throw new SignCryptoUtil.SignException("RSA 密钥对生成失败：" + e.getMessage(), e);
        }
    }

    private static KeyPairPem generateSm2() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", "BC");
            gen.initialize(new java.security.spec.ECGenParameterSpec("sm2p256v1"), new SecureRandom());
            KeyPair kp = gen.generateKeyPair();
            return new KeyPairPem(PemCodecUtil.toPem(kp.getPublic()), PemCodecUtil.toPem(kp.getPrivate()));
        } catch (Exception e) {
            throw new SignCryptoUtil.SignException("SM2 密钥对生成失败：" + e.getMessage(), e);
        }
    }

    public record KeyPairPem(String publicPem, String privatePem) {
    }
}
