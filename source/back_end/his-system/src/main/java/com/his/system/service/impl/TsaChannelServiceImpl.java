package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.system.entity.BizTsaToken;
import com.his.system.entity.SysTsaServer;
import com.his.system.mapper.BizTsaTokenMapper;
import com.his.system.mapper.SysTsaServerMapper;
import com.his.system.service.RedisSequenceService;
import com.his.system.service.TsaChannelService;
import com.his.common.util.*;
import com.his.system.utils.KeyPairFactory;
import com.his.system.utils.KeyProtectorUtil;
import com.his.system.utils.SignCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 可信时间戳通道（本地内置 TSA —— G6 的演示信任根）。
 */
@Slf4j
@Service
public class TsaChannelServiceImpl implements TsaChannelService {
    /**
     * 令牌规范化串版本前缀（改格式必须换版本号，否则历史令牌验不过）
     */
    private static final String CANONICAL_PREFIX = "HIS-TSA-V1";
    private static final String SERIAL_PREFIX = "TSA";
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
                + "|" + DateFormats.ISO_DATETIME.format(tsaTime);
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
        SysTsaServer server = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, TSA_CODE));
        if (server == null) {
            return false;
        }
        return SignCryptoUtil.verify(server.getPublicPem(),
                canonical(serial, digestHex, TimeUtil.toSeconds(tsaTime)), tokenValue);
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
