package com.his.ca.sdk;

import  org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * CA 推送验签工具：SM2 非对称签名验签。
 *
 * <p>使用方式：
 * <pre>{@code
 * CaPushVerifier verifier = new CaPushVerifier(caPublicKeyHex);
 * boolean ok = verifier.verify(timestamp, nonce, bodyBytes, signatureBase64);
 * }</pre>
 *
 * <p>CA 侧用推送私钥做 SM3withSM2 签名，外部系统用这把公钥验签。
 * 公钥是公开的，泄露也只能验签不能伪造推送。</p>
 */
public class CaPushVerifier {

    private final ECPublicKeyParameters publicKey;

    /**
     * 用 CA 推送公钥构造验签器。
     *
     * @param publicKeyHex SM2 公钥 hex（130 字符，04 前缀），从 CA 启动日志获取
     */
    public CaPushVerifier(String publicKeyHex) {
        if (publicKeyHex == null || publicKeyHex.isBlank()) {
            throw new IllegalArgumentException("CA 推送公钥不能为空");
        }
        X9ECParameters sm2Params = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domain = new ECDomainParameters(
                sm2Params.getCurve(), sm2Params.getG(), sm2Params.getN(), sm2Params.getH());
        byte[] pubKeyBytes = Hex.decode(publicKeyHex.trim());
        ECPoint pubPoint = domain.getCurve().decodePoint(pubKeyBytes);
        this.publicKey = new ECPublicKeyParameters(pubPoint, domain);
    }

    /**
     * 验签一次 CA 推送请求。
     *
     * @param timestamp      请求头 X-Ca-Timestamp
     * @param nonce          请求头 X-Ca-Nonce
     * @param bodyBytes      原始请求体字节（必须是原样字节，不能反序列化后再序列化）
     * @param signatureBase64 请求头 X-Ca-Signature（Base64 编码）
     * @return true=签名有效；false=签名无效（内容被改过，或不是 CA 签的）
     */
    public boolean verify(String timestamp, String nonce, byte[] bodyBytes, String signatureBase64) {
        if (timestamp == null || nonce == null || signatureBase64 == null) {
            return false;
        }
        String canonical = timestamp.trim() + "\n" + nonce.trim() + "\n" + sha256Hex(bodyBytes);
        try {
            SM2Signer signer = new SM2Signer();
            signer.init(false, publicKey);
            byte[] data = canonical.getBytes(StandardCharsets.UTF_8);
            signer.update(data, 0, data.length);
            return signer.verifySignature(Base64.getDecoder().decode(signatureBase64.trim()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 公钥指纹前 16 位，用于和请求头 X-Ca-Key-Id 比对，防止 CA 换了推送密钥后还在用旧公钥验签。
     */
    public String keyId() {
        byte[] encoded = publicKey.getQ().getEncoded(false);
        return Hex.toHexString(encoded).substring(0, 16);
    }

    private static String sha256Hex(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            return Hex.toHexString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
