package com.his.security;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.DigestUtil;
import com.his.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 登录口令<b>传输加密</b>的唯一实现点：SM2 国密（C1C3C2），负责把前端密文还原成明文口令。
 *
 * <p><b>只解决"口令在网络上不能是明文"这一个问题，不改落库形态</b> ——
 * 还原出的明文照旧交给 Spring Security 去做 {@code BCryptPasswordEncoder.matches}，
 * {@code sys_user.password} 里那 480 条 {@code $2a$10$...} 一条都不用动。
 *
 * <h3>跨语言互操作（踩过的坑，改前先读）</h3>
 * <ol>
 *   <li><b>密文要不要 {@code 04} 前缀</b>：C1 是椭圆曲线点，标准是未压缩格式带 {@code 04} 前缀（65 字节）。
 *       但 sm-crypto（PC 端和小程序用的库）输出的 C1 <b>永远不带</b>前缀（64 字节）。
 *       BouncyCastle 只认带前缀的，不带就直接抛 {@code Invalid point encoding}。
 *       所以 {@link #decrypt(String)} 解密前<b>无条件</b>补 {@code 04}。
 *       千万别用 {@code startsWith("04")} 判断：x 坐标首字节随机，约 1/256 的密文恰好以 04 开头，
 *       误判后解密必失败，症状是「登录时好时坏」（实测踩过）。</li>
 *   <li><b>密文块顺序</b>：国密 GB/T 35276 推荐 C1C3C2，sm-crypto 默认也是它。
 *       后端必须 {@code setMode(SM2Engine.Mode.C1C3C2)}，写错顺序会抛 {@code invalid cipher text}。</li>
 *   <li><b>反向</b>：Java 端加密出来是 <b>带</b> {@code 04} 的 103 字节，Node 端要解就得剥掉前缀。本次只有前端加密、后端解密，用不到。</li>
 * </ol>
 *
 * <h3>线程安全</h3>
 * BouncyCastle 的 {@code SM2Engine} 内部有块状态，不是线程安全的；hutool 的 {@code SM2} 实例持有同一个 engine。
 * 因此这里<b>每次加解密都 new 一个 SM2</b>（构造只做参数解析，开销远低于一次 SM2 椭圆曲线运算），
 * 既保证并发安全也不用 ThreadLocal 占着不放。
 *
 * <h3>做不到的事（别指望它）</h3>
 * SM2 每次加密随机选 k，同一明文两次密文不同，能防"比对密文猜密码"；
 * 但抓到一个完整登录请求照样能重放拿到 token。<b>根治要靠 HTTPS</b>，本类只是 HTTP 环境下的缓解手段。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordCipher {

    /** 未压缩公钥点前缀 */
    private static final String C1_PREFIX = "04";
    /** C1(64B,无04前缀) + C3(32B) = 96 字节；最短明文 1 字节 */
    private static final int MIN_CIPHER_HEX_LEN = 2 * (96 + 1);
    /** 口令最长 64 字节（UTF-8），再长就是有人拿接口当存储用了 */
    private static final int MAX_PLAIN_BYTES = 64;
    private static final int MAX_CIPHER_HEX_LEN = 2 * (96 + MAX_PLAIN_BYTES);

    private static final Pattern HEX = Pattern.compile("^[0-9a-fA-F]+$");

    private final PasswordCryptoProperties properties;

    @Getter
    private volatile String privateKeyHex;
    @Getter
    private volatile String publicKeyHex;
    /** 公钥指纹，前端用来判断"公钥换了要不要重拉" */
    @Getter
    private volatile String keyId;

    @PostConstruct
    public void init() {
        String prv = trimToNull(properties.getPrivateKey());
        if (prv == null) {
            String[] pair = generateKeyPair();
            prv = pair[0];
            this.privateKeyHex = prv;
            this.publicKeyHex = pair[1];
            log.warn("[登录加密] 未配置 his.security.sm2.private-key，已临时生成一对 SM2 密钥（重启即失效）。"
                    + "多实例部署必须显式配置同一对，否则会出现随机的登录失败。");
        } else {
            this.privateKeyHex = prv.toLowerCase();
            String pub = trimToNull(properties.getPublicKey());
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
        if (cipherHex == null || cipherHex.isBlank()) {
            throw new BusinessException("登录密码不能为空");
        }
        String cipher = cipherHex.trim();
        // 明文口令（"admin / 123456" 这类）在这里就该被挡回去 ——
        // 不是因为解不开，而是"允许明文通过"等于这套加密白做，边界必须硬。
        if (!HEX.matcher(cipher).matches()
                || cipher.length() % 2 != 0
                || cipher.length() < MIN_CIPHER_HEX_LEN
                || cipher.length() > MAX_CIPHER_HEX_LEN) {
            throw new BusinessException("登录密码未加密传输，已被拒绝，请刷新页面后重试");
        }

        // sm-crypto 输出的 C1 永远不带 04 前缀（64 字节 x|y），这里无条件补上。
        // 不能用 startsWith("04") 做「智能判断」：C1 的 x 坐标首字节是均匀随机的，
        // 约 1/256 的密文恰好以 04 开头，被误判为已带前缀后 BouncyCastle 解出
        // Invalid point coordinates —— 症状是「登录绝大多数正常、偶尔莫名失败」，极难排查。
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
            // 最常见成因：后端换了密钥（重启生成了新 key），而前端还拿着旧公钥加密
            log.warn("[登录加密] SM2 解密失败，keyId={}，密文长度={}，原因={}（多半是公钥已轮换，前端需重拉）",
                    keyId, cipher.length(), e.getMessage());
            throw new BusinessException("登录密码密文解析失败，请刷新页面后重试");
        }
    }

    // ---------------- 密钥构造 ----------------

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

    /** 由私钥推导公钥：Q = dG */
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

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}
