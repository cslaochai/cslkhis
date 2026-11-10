package com.his.ca.sdk;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * CA 对外接口的客户端签名工具（SM2 非对称签名）。
 *
 * <p>调用方持私钥签名，CA 用登记的公钥验签。签名串与 CA 的
 * {@code CaApiAuthFilter} 逐字一致：
 * <pre>
 * appId \n timestamp \n nonce \n METHOD \n pathWithQuery \n sha256Hex(body)
 * </pre>
 *
 * <p>与推送验签同一算法（SM3withSM2）与同一编码（签名值 Base64、公钥 130 hex 04 前缀），
 * 这样一套国密能力同时覆盖「CA 推给 HIS」和「HIS 调 CA」两个方向。</p>
 */
public final class CaRequestSigner {

    private static final ECDomainParameters DOMAIN;

    static {
        X9ECParameters params = GMNamedCurves.getByName("sm2p256v1");
        DOMAIN = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    private CaRequestSigner() {
    }

    /**
     * 构造待签名串（六行，LF 分隔，末尾不加换行）。
     */
    public static String canonical(String appId, String timestamp, String nonce,
                                   String method, String pathWithQuery, String bodySha256Hex) {
        return appId + "\n" + timestamp + "\n" + nonce + "\n"
                + method.toUpperCase() + "\n" + pathWithQuery + "\n" + bodySha256Hex;
    }

    /**
     * 用私钥对签名串签名，返回 Base64 签名值（对应请求头 X-Signature）。
     *
     * @param privateKeyHex SM2 私钥 hex（64 位十六进制，可带前导零）
     * @param canonical     待签名串
     */
    public static String sign(String privateKeyHex, String canonical) {
        if (privateKeyHex == null || privateKeyHex.isBlank()) {
            throw new IllegalArgumentException("SM2 私钥不能为空");
        }
        String key = privateKeyHex.trim().toLowerCase();
        if (key.startsWith("0x")) {
            key = key.substring(2);
        }
        if (key.length() > 64) {
            throw new IllegalArgumentException("SM2 私钥应为 64 位十六进制");
        }
        try {
            ECPrivateKeyParameters privateKey =
                    new ECPrivateKeyParameters(new BigInteger(key, 16), DOMAIN);
            SM2Signer signer = new SM2Signer();
            signer.init(true, privateKey);
            byte[] data = canonical.getBytes(StandardCharsets.UTF_8);
            signer.update(data, 0, data.length);
            return Base64.getEncoder().encodeToString(signer.generateSignature());
        } catch (Exception e) {
            throw new IllegalStateException("SM2 签名失败：" + e.getMessage(), e);
        }
    }

    /**
     * 由私钥推导公钥（130 字符 hex，04 前缀）——登记到 CA 用的就是它。
     */
    public static String publicKeyOf(String privateKeyHex) {
        String key = privateKeyHex.trim().toLowerCase();
        if (key.startsWith("0x")) {
            key = key.substring(2);
        }
        ECPoint q = DOMAIN.getG().multiply(new BigInteger(key, 16)).normalize();
        return Hex.toHexString(q.getEncoded(false));
    }

    /**
     * 公钥指纹前 16 位，便于和 CA 端登记的公钥核对。
     */
    public static String keyId(String privateKeyHex) {
        return publicKeyOf(privateKeyHex).substring(0, 16);
    }

    public static String sha256Hex(byte[] data) {
        try {
            return Hex.toHexString(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
