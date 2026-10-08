package com.his.system.support;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.DigestUtil;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.config.PasswordCryptoProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 登录口令传输加密的唯一实现点：SM2 国密（C1C3C2），负责把前端密文还原成明文口令。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordCipherService {

    /**
     * 未压缩公钥点前缀
     */
    private static final String C1_PREFIX = "04";
    /**
     * C1(64B,无04前缀) + C3(32B) = 96 字节；最短明文 1 字节
     */
    private static final int MIN_CIPHER_HEX_LEN = 2 * (96 + 1);
    /**
     * 口令最长 64 字节（UTF-8），再长就是有人拿接口当存储用了
     */
    private static final int MAX_PLAIN_BYTES = 64;
    private static final int MAX_CIPHER_HEX_LEN = 2 * (96 + MAX_PLAIN_BYTES);

    private static final Pattern HEX = Pattern.compile("^[0-9a-fA-F]+$");

    private final PasswordCryptoProperties passwordCryptoProperties;

    @Getter
    private volatile String privateKeyHex;
    @Getter
    private volatile String publicKeyHex;
    /**
     * 公钥指纹，前端用来判断"公钥换了要不要重拉"
     */
    @Getter
    private volatile String keyId;

    /**
     * 密钥构造
     *
     * @return
     */
    private static String[] generateKeyPair() {
        ECDomainParameters domain = sm2Domain();
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(new ECKeyGenerationParameters(domain, new SecureRandom()));
        AsymmetricCipherKeyPair pair = generator.generateKeyPair();
        ECPrivateKeyParameters priv = (ECPrivateKeyParameters) pair.getPrivate();
        ECPublicKeyParameters pub = (ECPublicKeyParameters) pair.getPublic();
        String prvHex = leftPad64(priv.getD().toString(16));
        return new String[]{prvHex, pointToHex(pub.getQ())};
    }

    /**
     * 由私钥推导公钥：Q = dG
     */
    private static String derivePublicKey(String privateKeyHex) {
        ECDomainParameters domain = sm2Domain();
        ECPoint q = domain.getG().multiply(new BigInteger(privateKeyHex, 16)).normalize();
        return pointToHex(q);
    }

    private static String pointToHex(ECPoint point) {
        return C1_PREFIX
                + leftPad64(point.getAffineXCoord().toBigInteger().toString(16))
                + leftPad64(point.getAffineYCoord().toBigInteger().toString(16));
    }

    private static ECDomainParameters sm2Domain() {
        X9ECParameters params = GMNamedCurves.getByName("sm2p256v1");
        return new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
    }

    private static String leftPad64(String hex) {
        if (hex.length() >= 64) {
            return hex;
        }
        return "0".repeat(64 - hex.length()) + hex;
    }

    @PostConstruct
    public void init() {
        String prv = TextUtil.trimToNull(passwordCryptoProperties.getPrivateKey());
        if (prv == null) {
            String[] pair = generateKeyPair();
            prv = pair[0];
            this.privateKeyHex = prv;
            this.publicKeyHex = pair[1];
            log.warn("[登录加密] 未配置 his.security.sm2.private-key，已临时生成一对 SM2 密钥（重启即失效）。"
                    + "多实例部署必须显式配置同一对，否则会出现随机的登录失败。");
        } else {
            this.privateKeyHex = prv.toLowerCase();
            String pub = TextUtil.trimToNull(passwordCryptoProperties.getPublicKey());
            this.publicKeyHex = (pub == null ? derivePublicKey(prv) : pub.toLowerCase());
        }
        this.keyId = DigestUtil.sha256Hex(this.publicKeyHex).substring(0, 16);
        log.info("[登录加密] SM2 就绪，keyId={}，公钥={}...", keyId, publicKeyHex.substring(0, 16));
    }

    /**
     * 解密前端传来的口令密文。
     *
     * @param cipherHex sm-crypto 的 {@code doEncrypt} 输出（十六进制，不带 04 前缀）
     * @return 明文口令
     */
    public String decrypt(String cipherHex) {
        if (!TextUtil.hasText(cipherHex)) {
            throw new BusinessException("登录密码不能为空");
        }
        String cipher = cipherHex.trim();
        if (!HEX.matcher(cipher).matches()
                || cipher.length() % 2 != 0
                || cipher.length() < MIN_CIPHER_HEX_LEN
                || cipher.length() > MAX_CIPHER_HEX_LEN) {
            throw new BusinessException("登录密码未加密传输，已被拒绝，请刷新页面后重试");
        }

        String fullHex = C1_PREFIX + cipher;
        try {
            SM2 sm2 = new SM2(privateKeyHex, publicKeyHex);
            sm2.setMode(SM2Engine.Mode.C1C3C2);
            byte[] plain = sm2.decrypt(HexUtil.decodeHex(fullHex), KeyType.PrivateKey);
            if (plain == null || plain.length == 0) {
                throw new BusinessException("登录密码密文解析失败，请刷新页面后重试");
            }
            String password = new String(plain, StandardCharsets.UTF_8);
            if (password.isEmpty()) {
                throw new BusinessException("登录密码密文解析失败，请刷新页面后重试");
            }
            return password;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[登录加密] SM2 解密失败，keyId={}，密文长度={}，原因={}（多半是公钥已轮换，前端需重拉）",
                    keyId, cipher.length(), e.getMessage());
            throw new BusinessException("登录密码密文解析失败，请刷新页面后重试");
        }
    }

}
