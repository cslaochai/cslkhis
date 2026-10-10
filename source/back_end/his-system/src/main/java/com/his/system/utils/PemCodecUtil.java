package com.his.system.utils;

import com.his.common.util.TextUtil;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * PEM 编解码。只支持 JDK 原生生成的两种格式：
 */
public final class PemCodecUtil {

    private static final String PUBLIC_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_FOOTER = "-----END PUBLIC KEY-----";
    private static final String PRIVATE_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_FOOTER = "-----END PRIVATE KEY-----";

    public static String toPem(PublicKey key) {
        return wrap(PUBLIC_HEADER, PUBLIC_FOOTER, key.getEncoded());
    }

    public static String toPem(PrivateKey key) {
        return wrap(PRIVATE_HEADER, PRIVATE_FOOTER, key.getEncoded());
    }

    public static PublicKey parsePublicKey(String pem) {
        try {
            byte[] der = unwrap(pem);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalArgumentException("公钥 PEM 解析失败：" + e.getMessage(), e);
        }
    }

    public static PrivateKey parsePrivateKey(String pem) {
        try {
            byte[] der = unwrap(pem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalArgumentException("私钥 PEM 解析失败：" + e.getMessage(), e);
        }
    }

    private static String wrap(String header, String footer, byte[] der) {
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der);
        return header + "\n" + body + "\n" + footer;
    }

    private static byte[] unwrap(String pem) {
        if (!TextUtil.hasText(pem)) {
            throw new IllegalArgumentException("PEM 内容为空");
        }
        String body = pem
                .replace(PUBLIC_HEADER, "").replace(PUBLIC_FOOTER, "")
                .replace(PRIVATE_HEADER, "").replace(PRIVATE_FOOTER, "")
                .replaceAll("\\s", "");
        if (body.isEmpty()) {
            throw new IllegalArgumentException("PEM 正文为空（未找到 Base64 内容，可能是头尾标签不匹配）");
        }
        return Base64.getDecoder().decode(body);
    }
}
