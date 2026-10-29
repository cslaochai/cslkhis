package com.his.platform.utils;

import com.his.common.util.TextUtil;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * PEM 编解码。支持 RSA（JDK）与 SM2/EC（BC provider，识别 sm2p256v1 国密曲线 OID）。
 * 按 SPKI / PKCS#8 里的算法 OID 选择解析通道：RSA 走 JDK，EC（含 SM2）走 BC。
 */
public final class PemCodecUtil {

    static {
        // SM2/EC 解析依赖 BC provider（sm2p256v1 命名曲线不在 JDK SunEC 注册表）；
        // 幂等注册，防止与 hutool 等其它组件的注册冲突
        if (java.security.Security.getProvider("BC") == null) {
            java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private static final String PUBLIC_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_FOOTER = "-----END PUBLIC KEY-----";
    private static final String PRIVATE_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_FOOTER = "-----END PRIVATE KEY-----";

    /** RSA 算法 OID（1.2.840.113549.1.1.1） */
    private static final String OID_RSA = "1.2.840.113549.1.1.1";
    /** EC 算法 OID（1.2.840.10045.2.1），SM2 推荐曲线（1.2.156.10197.1.301）是其曲线参数 */
    private static final String OID_EC = "1.2.840.10045.2.1";

    private PemCodecUtil() {
    }

    public static String toPem(PublicKey key) {
        return wrap(PUBLIC_HEADER, PUBLIC_FOOTER, key.getEncoded());
    }

    /**
     * 从 X.509 证书 PEM 中取出公钥（SPKI 格式 PEM）。
     *
     * <p>用于 CA 线下签发回填时校验「证书里的公钥」与「本地密钥对」是否同一对——
     * 不一致说明贴错了证书，回填了也验不过签。
     *
     * @return 公钥 PEM；证书无法解析时返回 null（由调用方给出人话报错）
     */
    public static String publicKeyFromCertificate(String certPem) {
        if (certPem == null || certPem.isBlank()) {
            return null;
        }
        try {
            java.security.cert.CertificateFactory cf =
                    java.security.cert.CertificateFactory.getInstance("X.509");
            try (java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(
                    certPem.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
                java.security.cert.Certificate cert = cf.generateCertificate(in);
                PublicKey pub = cert.getPublicKey();
                return pub == null ? null : toPem(pub);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String toPem(PrivateKey key) {
        return wrap(PRIVATE_HEADER, PRIVATE_FOOTER, key.getEncoded());
    }

    public static PublicKey parsePublicKey(String pem) {
        try {
            byte[] der = unwrap(pem);
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(der);
            String oid = spki.getAlgorithm().getAlgorithm().getId();
            if (OID_RSA.equals(oid)) {
                return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
            }
            if (OID_EC.equals(oid)) {
                // SM2 曲线 OID 不在 JDK SunEC 注册表，必须走 BC（bcprov 已含 sm2p256v1 命名曲线）
                return KeyFactory.getInstance("EC", "BC").generatePublic(new X509EncodedKeySpec(der));
            }
            throw new IllegalArgumentException("公钥 PEM 解析失败：不支持的算法 OID " + oid + "（仅支持 RSA / SM2）");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("公钥 PEM 解析失败：" + e.getMessage(), e);
        }
    }

    public static PrivateKey parsePrivateKey(String pem) {
        try {
            byte[] der = unwrap(pem);
            PrivateKeyInfo pki = PrivateKeyInfo.getInstance(der);
            String oid = pki.getPrivateKeyAlgorithm().getAlgorithm().getId();
            if (OID_RSA.equals(oid)) {
                return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
            }
            if (OID_EC.equals(oid)) {
                return KeyFactory.getInstance("EC", "BC").generatePrivate(new PKCS8EncodedKeySpec(der));
            }
            throw new IllegalArgumentException("私钥 PEM 解析失败：不支持的算法 OID " + oid + "（仅支持 RSA / SM2）");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("私钥 PEM 解析失败：" + e.getMessage(), e);
        }
    }

    /** 由密钥对象推断算法标识：EC（含 SM2）→ "EC"，RSA → "RSA" */
    public static String keyAlgorithm(java.security.Key key) {
        return key == null ? null : key.getAlgorithm();
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
