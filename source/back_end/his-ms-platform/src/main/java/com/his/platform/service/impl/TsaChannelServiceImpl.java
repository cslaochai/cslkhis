package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.util.DateFormats;
import com.his.common.util.TimeUtil;
import com.his.platform.config.SignProperties;
import com.his.platform.entity.BizTsaToken;
import com.his.platform.entity.SysTsaServer;
import com.his.platform.mapper.BizTsaTokenMapper;
import com.his.platform.mapper.SysTsaServerMapper;
import com.his.platform.service.RedisSequenceService;
import com.his.platform.service.TsaChannelService;
import com.his.platform.support.CssslCaClient;
import com.his.platform.utils.KeyPairFactory;
import com.his.platform.utils.KeyProtectorUtil;
import com.his.platform.utils.SignCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 可信时间戳通道：病历签名授时与令牌验真的时间来源出口。
 * <ul>
 *   <li><b>external</b>（默认建议）：csssl 院内 TSA —— 独立 SM2 密钥签发 SM3withSM2 令牌，
 *       令牌规范化串 {@code CSSSL-TSA-V1|serial|digestHex|tsaTime}（与 csssl 一致），本地落台账。</li>
 *   <li><b>internal</b>：本地内置 TSA（院内自签信任根，演示过渡），令牌结构真实、可对抗本机时钟篡改。</li>
 * </ul>
 */
@Slf4j
@Service
public class TsaChannelServiceImpl implements TsaChannelService {
    /**
     * 令牌规范化串版本前缀：external（csssl）与 internal（本地内置）各一套，互不混用
     * —— 换格式必须换版本号，否则历史令牌验不过
     */
    private static final String CANONICAL_PREFIX_EXTERNAL = "CSSSL-TSA-V1";
    private static final String CANONICAL_PREFIX_INTERNAL = "HIS-TSA-V1";
    private static final String SERIAL_PREFIX = "TSA";
    private static final int MAX_RETRY = 3;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SysTsaServerMapper serverMapper;
    private final BizTsaTokenMapper tokenMapper;
    private final KeyProtectorUtil keyProtectorUtil;
    private final RedisSequenceService sequenceService;
    private final CssslCaClient cssslCaClient;
    private final SignProperties signProperties;

    /**
     * 就绪的服务行缓存（internal 模式：密钥轮换不在本期范围；重启进程即重读）
     */
    private volatile SysTsaServer cachedServer;
    private volatile String cachedPrivatePem;

    /**
     * csssl TSA 公钥缓存（external 模式）
     */
    private volatile String cachedTsaPublicPem;

    public TsaChannelServiceImpl(SysTsaServerMapper serverMapper, BizTsaTokenMapper tokenMapper,
                                 KeyProtectorUtil keyProtectorUtil, RedisSequenceService sequenceService,
                                 CssslCaClient cssslCaClient, SignProperties signProperties) {
        this.serverMapper = serverMapper;
        this.tokenMapper = tokenMapper;
        this.keyProtectorUtil = keyProtectorUtil;
        this.sequenceService = sequenceService;
        this.cssslCaClient = cssslCaClient;
        this.signProperties = signProperties;
        log.info("[TSA通道] 初始化 tsaMode={} caBaseUrl={}", signProperties.getTsaMode(), signProperties.getCaBaseUrl());
    }

    private boolean external() {
        return "external".equalsIgnoreCase(signProperties.getTsaMode());
    }

    /** 外部（csssl）令牌规范化串——与 csssl 的 TsaService.canonical 同一实现，两处必须逐字一致 */
    private static String canonicalExternal(String serial, String digestHex, LocalDateTime tsaTime) {
        return CANONICAL_PREFIX_EXTERNAL + "|" + serial + "|" + digestHex.toLowerCase()
                + "|" + ISO.format(tsaTime);
    }

    /** 本地内置令牌规范化串（保持历史令牌兼容，勿改格式） */
    private static String canonicalInternal(String serial, String digestHex, LocalDateTime tsaTime) {
        return CANONICAL_PREFIX_INTERNAL + "|" + serial + "|" + digestHex.toLowerCase()
                + "|" + DateFormats.ISO_DATETIME.format(tsaTime);
    }

    /**
     * 通道名称（页面展示）
     */
    public String name() {
        if (external()) {
            return "CSSSL院内TSA（国密时间戳）";
        }
        SysTsaServer s = peekServer();
        return s != null ? s.getTsaName() : "本地内置TSA";
    }

    public boolean available() {
        try {
            return external() ? externalAvailable() : readyServer() != null;
        } catch (Exception e) {
            log.warn("{}TSA未就绪（{}）——签名时间来源将按「本机时钟」处理",
                    external() ? "外部" : "本地", e.getMessage());
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
            return external() ? externalStamp(digestHex) : doStamp(digestHex);
        } catch (Exception e) {
            log.error("TSA盖章失败：{} —— 本次签名降级为本机时钟", e.getMessage());
            return null;
        }
    }

    /**
     * 校验一枚令牌是否由本通道签发、且与摘要 / 时刻一致（异常一律按"校验失败"处理）
     */
    public boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue) {
        try {
            return external() ? externalVerify(serial, digestHex, tsaTime, tokenValue)
                    : doVerifyToken(serial, digestHex, tsaTime, tokenValue);
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
        cachedTsaPublicPem = null;
    }

    // ---------- external（csssl 院内 TSA） ----------

    /** 探活：能取到 TSA 公钥即视为在线，并缓存公钥 */
    private boolean externalAvailable() {
        tsaPublicPem();
        return true;
    }

    private String tsaPublicPem() {
        String pem = cachedTsaPublicPem;
        if (pem != null) {
            return pem;
        }
        Map<String, String> info = cssslCaClient.tsaPublicKey();
        String fetched = info.get("publicKeyPem");
        if (fetched == null || fetched.isBlank()) {
            throw new IllegalStateException("csssl TSA 公钥为空");
        }
        cachedTsaPublicPem = fetched;
        return fetched;
    }

    /** 远程盖章：调 csssl /tsa/stamp，令牌与时刻以 TSA 回执为准，本地落台账供页面查验 */
    private Stamp externalStamp(String digestHex) {
        CssslCaClient.TsaStamp remote = cssslCaClient.stampTsa(digestHex);
        LocalDateTime tsaTime = LocalDateTime.parse(remote.tsaTime(), ISO);

        BizTsaToken row = new BizTsaToken();
        row.setSerial(remote.serial());
        row.setDigestHex(digestHex.toLowerCase());
        row.setTsaTime(tsaTime);
        row.setTokenValue(remote.tokenValue());
        row.setAlgo(remote.algo().isBlank() ? SignCryptoUtil.SM2_SIGN_ALGO : remote.algo());
        try {
            tokenMapper.insert(row);
        } catch (DuplicateKeyException e) {
            // 台账唯一键冲突说明令牌序列号重复（外部 TSA 异常）——必须让本次签名降级，
            // 绝不能返回"盖章成功"却留下与台账对不上的签名记录
            log.warn("TSA令牌序列号冲突（外部TSA）serial={}，本次签名降级本机时钟", remote.serial());
            return null;
        }
        log.info("TSA盖章成功（external）serial={} 时刻={} 算法={}", remote.serial(), tsaTime, row.getAlgo());
        return new Stamp(remote.serial(), tsaTime, remote.tokenValue());
    }

    /** 外部验真：台账断言（序列号存在 + 摘要一致）+ TSA 公钥验令牌签名 */
    private boolean externalVerify(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue) {
        if (serial == null || digestHex == null || tsaTime == null || tokenValue == null) {
            return false;
        }
        BizTsaToken ledger = tokenMapper.selectOne(new LambdaQueryWrapper<BizTsaToken>()
                .eq(BizTsaToken::getSerial, serial));
        if (ledger == null) {
            return false;
        }
        if (!digestHex.equalsIgnoreCase(ledger.getDigestHex())) {
            return false;
        }
        return SignCryptoUtil.verify(tsaPublicPem(),
                canonicalExternal(serial, digestHex, TimeUtil.toSeconds(tsaTime)), tokenValue);
    }

    // ---------- internal（本地内置 TSA，演示信任根） ----------

    private Stamp doStamp(String digestHex) {
        SysTsaServer server = readyServer();
        String privatePem = privatePemOf(server);
        String keyAlgo = signProperties.getKeyAlgorithm();

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            LocalDateTime tsaTime = TimeUtil.nowSeconds();
            String serial;
            try {
                long seq = sequenceService.next("TSA_TOKEN");
                serial = SERIAL_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE) + String.format("%06d", seq);
            } catch (Exception e) {
                serial = SERIAL_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)
                        + String.format("%06d", tokenMapper.countBySerialPrefix(
                        SERIAL_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)) + 1);
            }
            String token = SignCryptoUtil.sign(privatePem, canonicalInternal(serial, digestHex, tsaTime));

            BizTsaToken row = new BizTsaToken();
            row.setSerial(serial);
            row.setDigestHex(digestHex);
            row.setTsaTime(tsaTime);
            row.setTokenValue(token);
            row.setAlgo(SignCryptoUtil.signAlgoFor(keyAlgo));
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
        SysTsaServer server = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, TSA_CODE));
        if (server == null) {
            return false;
        }
        return SignCryptoUtil.verify(server.getPublicPem(),
                canonicalInternal(serial, digestHex, TimeUtil.toSeconds(tsaTime)), tokenValue);
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

    /** 自举本地 TSA：密钥算法跟随签名证书配置（SM2 或 RSA2048），私钥主口令加密落库 */
    private SysTsaServer bootstrap() {
        keyProtectorUtil.requireSecret();
        KeyPairFactory.KeyPairPem kp = KeyPairFactory.generate(signProperties.getKeyAlgorithm());
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
