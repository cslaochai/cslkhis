package com.his.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * PEM 编解码。只支持 JDK 原生生成的两种格式：
 * <ul>
 *   <li>公钥：{@code -----BEGIN PUBLIC KEY-----}（X.509 SubjectPublicKeyInfo）</li>
 *   <li>私钥：{@code -----BEGIN PRIVATE KEY-----}（PKCS#8）</li>
 * </ul>
 * 刻意**不支持加密私钥 PEM**（{@code BEGIN ENCRYPTED PRIVATE KEY}）——
 * 私钥的静态保护统一由 {@link KeyProtectorUtil} 负责，两条路并存只会让人搞不清密钥到底怎么护的。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)

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
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("PEM 内容为空");
        }
        // 逐条去掉头尾标签（公钥/私钥各一套），再抹掉全部空白，剩下的就是 Base64 正文。
        //
        // 这里**不能**用「截取两个 ## 之间的内容」这种写法：把 BEGIN/END 替换成同一个哨兵字符后，
        // 一条贪婪正则 `##[^#]*##` 会从第一个哨兵一路吃到最后一个哨兵 ——
        // 也就是把整段正文连头带尾全删掉，剩下的 " PUBLIC KEY-----" 清掉符号后是 "PUBLICKEY"，
        // Base64 解码时报 "Last unit does not have enough valid bits"。
        // 那个报错指向的是"长度不对"，与真正的原因（正文被删空）差得很远，是典型的方向性误导。
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
