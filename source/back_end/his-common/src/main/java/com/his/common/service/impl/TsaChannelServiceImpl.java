package com.his.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.service.RedisSequenceService;
import com.his.common.entity.BizTsaToken;
import com.his.common.entity.SysTsaServer;
import com.his.common.mapper.BizTsaTokenMapper;
import com.his.common.mapper.SysTsaServerMapper;
import com.his.common.service.TsaChannelService;
import com.his.common.util.KeyPairFactory;
import com.his.common.util.KeyProtectorUtil;
import com.his.common.util.SignCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 可信时间戳通道（本地内置 TSA —— G6 的演示信任根）。
 *
 * <p><b>真的什么、假的是什么</b>：
 * <ul>
 *   <li>真的部分：令牌结构真实 —— TSA 独立 RSA-2048 密钥（与员工证书不同根）、
 *       对「序列号+摘要+时刻」规范化串签名、台账只增不改、可独立验签。
 *       它能对抗"改本机时钟"：令牌里的时刻被 TSA 私钥签过，事后改库改不动签名值。</li>
 *   <li>假的部分：信任根仍是院内（服务行的密钥自己生成自己托管），
 *       系统管理员在技术上具备"重签一份令牌"的能力。**不是第三方 CA/TSA，
 *       不对外声称法律效力** —— 页面与 summary 必须如实标注这一点。</li>
 * </ul>
 *
 * <p><b>失败语义（两条硬约束）</b>：
 * <ol>
 *   <li>{@link #stamp} 失败**不许让签名失败** —— 任何异常都收敛成返回 {@code null} 并留 warn，
 *       调用方降级「本机时钟」。宁可承认不可信，也不谎报可信；但也不能因为时间戳服务抖动
 *       把医生的签名堵死。</li>
 *   <li>{@link #available()} 为 false 时，{@code sign.time_source=3} 的配置必须降级为 1
 *       （本机时钟）—— 没有可用的时间戳就绝不能写 time_source=3。</li>
 * </ol>
 *
 * <p>令牌的规范化内容由本类自定义（{@code HIS-TSA-V1|serial|digestHex|tsaTime}），
 * 验证方只依赖 {@link #verifyToken}，不解析令牌内部格式。
 * 接真第三方 TSA（RFC 3161 / 厂商 SDK）= 在本类内部按配置分支替换外发那一段，
 * 签名链、证书体系、验签断言都不动。
 *
 * <p><b>密钥自举</b>：首次使用时若服务表无 LOCAL 行，自动生成密钥对落库（与证书自动签发同思路）；
 * 私钥用 {@link KeyProtectorUtil} 加密托管，主口令缺失时 available()=false，签名侧自然降级本机时钟。
 */
@Slf4j
@Service
public class TsaChannelServiceImpl implements TsaChannelService {
    /**
     * 令牌规范化串版本前缀（改格式必须换版本号，否则历史令牌验不过）
     */
    private static final String CANONICAL_PREFIX = "HIS-TSA-V1";
    private static final String SERIAL_PREFIX = "TSA";
    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final int MAX_RETRY = 3;

    private final SysTsaServerMapper serverMapper;
    private final BizTsaTokenMapper tokenMapper;
    private final KeyProtectorUtil keyProtectorUtil;
    private final RedisSequenceService sequenceService;

    /**
     * 就绪的服务行缓存（密钥轮换不在本期范围；重启进程即重读）
     */
    private volatile SysTsaServer cachedServer;
    private volatile String cachedPrivatePem;

    public TsaChannelServiceImpl(SysTsaServerMapper serverMapper, BizTsaTokenMapper tokenMapper,
                                 KeyProtectorUtil keyProtectorUtil, RedisSequenceService sequenceService) {
        this.serverMapper = serverMapper;
        this.tokenMapper = tokenMapper;
        this.keyProtectorUtil = keyProtectorUtil;
        this.sequenceService = sequenceService;
    }

    /**
     * 令牌规范化串（与验签共用，两处必须同一实现）
     */
    private static String canonical(String serial, String digestHex, LocalDateTime tsaTime) {
        return CANONICAL_PREFIX + "|" + serial + "|" + digestHex.toLowerCase()
                + "|" + TS_FORMAT.format(tsaTime);
    }

    /**
     * 通道名称（页面展示，如"本地内置TSA（演示信任根）"）
     */
    public String name() {
        SysTsaServer s = peekServer();
        return s != null ? s.getTsaName() : "本地内置TSA";
    }

    public boolean available() {
        try {
            return readyServer() != null;
        } catch (Exception e) {
            log.warn("本地TSA未就绪（{}）——签名时间来源将按「本机时钟」处理", e.getMessage());
            return false;
        }
    }

    /**
     * 就绪时的通道名称，未就绪返回 null（页面按"未接入"展示）
     */
    public String readyName() {
        return available() ? name() : null;
    }

    /**
     * 对内容摘要盖可信时间戳。
     *
     * @param digestHex 被盖时间戳的内容摘要（SHA-256 十六进制小写）
     * @return 序列号 / TSA 授时时刻 / 令牌值；返回 {@code null} = 无可用 TSA，调用方降级本机时钟
     */
    public Stamp stamp(String digestHex) {
        try {
            if (!available()) {
                return null;
            }
            return doStamp(digestHex);
        } catch (Exception e) {
            log.error("TSA盖章失败：{} —— 本次签名降级为本机时钟", e.getMessage());
            return null;
        }
    }

    /**
     * 校验一枚令牌是否由本 TSA 签发、且与摘要 / 时刻一致（异常一律按"校验失败"处理）
     */
    public boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue) {
        try {
            return doVerifyToken(serial, digestHex, tsaTime, tokenValue);
        } catch (Exception e) {
            log.warn("TSA令牌校验异常：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 失效内部缓存（运维接口启停/配置变更后调用，下次读取重新查库，操作即时生效）
     */
    public void invalidate() {
        cachedServer = null;
        cachedPrivatePem = null;
    }

    private Stamp doStamp(String digestHex) {
        SysTsaServer server = readyServer();
        String privatePem = privatePemOf(server);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            LocalDateTime tsaTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            String serial;
            try {
                long seq = sequenceService.next("TSA_TOKEN");
                serial = SERIAL_PREFIX + LocalDate.now().format(NO_DATE) + String.format("%06d", seq);
            } catch (Exception e) {
                serial = SERIAL_PREFIX + LocalDate.now().format(NO_DATE)
                        + String.format("%06d", tokenMapper.countBySerialPrefix(
                        SERIAL_PREFIX + LocalDate.now().format(NO_DATE)) + 1);
            }
            String token = SignCryptoUtil.sign(privatePem, canonical(serial, digestHex, tsaTime));

            BizTsaToken row = new BizTsaToken();
            row.setSerial(serial);
            row.setDigestHex(digestHex);
            row.setTsaTime(tsaTime);
            row.setTokenValue(token);
            row.setAlgo(SignCryptoUtil.SIGN_ALGO);
            try {
                tokenMapper.insert(row);
                return new Stamp(serial, tsaTime, token);
            } catch (DuplicateKeyException e) {
                if (attempt == MAX_RETRY) {
                    throw new IllegalStateException("TSA令牌序列号连续冲突 " + MAX_RETRY + " 次（" + serial + "）", e);
                }
                log.warn("TSA令牌序列号冲突，重试第 {} 次：{}", attempt, serial);
            }
        }
        throw new IllegalStateException("TSA盖章失败");
    }

    private boolean doVerifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue) {
        if (serial == null || digestHex == null || tsaTime == null || tokenValue == null) {
            return false;
        }
        // 断言一：台账里有这枚序列号，且摘要一致（签名行摘要被改 → 与台账对不上）
        BizTsaToken ledger = tokenMapper.selectOne(new LambdaQueryWrapper<BizTsaToken>()
                .eq(BizTsaToken::getSerial, serial));
        if (ledger == null) {
            return false;
        }
        if (!digestHex.equalsIgnoreCase(ledger.getDigestHex())) {
            return false;
        }
        // 断言二：令牌签名值用 TSA 公钥可验、且盖的正是「传入的摘要+时刻」
        //（签名行 tsa_time / tsa_token 任何一个被改，这里都验不过）
        // G6b 修正：这里**刻意不走 readyServer()**——停用（tsa_status=0）的语义是
        // "不再签发新令牌"，历史令牌凭已登记的公钥仍必须可验；
        // 走 readyServer() 会在停用时抛异常 → 收敛成 false → 历史签名验签第三断言误判失败。
        SysTsaServer server = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, TSA_CODE));
        if (server == null) {
            return false;
        }
        return SignCryptoUtil.verify(server.getPublicPem(),
                canonical(serial, digestHex, tsaTime.truncatedTo(ChronoUnit.SECONDS)), tokenValue);
    }

    private SysTsaServer peekServer() {
        SysTsaServer s = cachedServer;
        if (s != null) {
            return s;
        }
        try {
            return readyServer();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取可用服务行：无则自举生成；停用视为未就绪
     */
    private synchronized SysTsaServer readyServer() {
        SysTsaServer s = cachedServer;
        if (s != null) {
            return s;
        }
        s = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, TSA_CODE));
        if (s == null) {
            s = bootstrap();
            log.info("本地TSA密钥自举完成 tsaName={} 指纹={}", s.getTsaName(), s.getKeyFingerprint());
        }
        if (!Integer.valueOf(1).equals(s.getTsaStatus())) {
            throw new IllegalStateException("TSA服务已停用（tsa_code=" + TSA_CODE + "）");
        }
        cachedServer = s;
        cachedPrivatePem = null;
        return s;
    }

    private SysTsaServer bootstrap() {
        keyProtectorUtil.requireSecret();
        KeyPairFactory.KeyPairPem kp = KeyPairFactory.generate();
        String salt = keyProtectorUtil.newSalt();
        int iterations = 120000;

        SysTsaServer s = new SysTsaServer();
        s.setTsaCode(TSA_CODE);
        s.setTsaName("本地内置TSA（演示信任根，非第三方）");
        s.setPublicPem(kp.publicPem());
        s.setKeyFingerprint(SignCryptoUtil.fingerprint(kp.publicPem()));
        s.setProtectedPrivateKey(keyProtectorUtil.protect(kp.privatePem(), salt, iterations));
        s.setKeySalt(salt);
        s.setKeyIterations(iterations);
        s.setTsaStatus(1);
        s.setRemark("G6 本地内置TSA：令牌结构真实、可对抗本机时钟篡改；信任根为院内，不对外声称法律效力");
        try {
            serverMapper.insert(s);
        } catch (DuplicateKeyException e) {
            // 并发自举：另一线程已插入，读它的
            s = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                    .eq(SysTsaServer::getTsaCode, TSA_CODE));
            if (s == null) {
                throw new IllegalStateException("TSA服务行并发创建失败");
            }
        }
        return s;
    }

    private String privatePemOf(SysTsaServer server) {
        String pem = cachedPrivatePem;
        if (pem != null) {
            return pem;
        }
        pem = keyProtectorUtil.unprotect(server.getProtectedPrivateKey(), server.getKeySalt(),
                server.getKeyIterations());
        cachedPrivatePem = pem;
        return pem;
    }
}
