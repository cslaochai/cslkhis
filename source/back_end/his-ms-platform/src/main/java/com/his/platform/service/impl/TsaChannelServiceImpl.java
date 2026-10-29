package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.util.TimeUtil;
import com.his.platform.config.SignProperties;
import com.his.platform.entity.BizTsaToken;
import com.his.platform.mapper.BizTsaTokenMapper;
import com.his.platform.service.TsaChannelService;
import com.his.platform.support.CssslCaClient;
import com.his.platform.utils.SignCryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 可信时间戳（TSA）通道：病历签名授时与令牌验真的时间来源出口。
 *
 * <p>TSA 服务（独立 SM2 时间戳密钥、令牌签发、验签、审计）由 CA 认证中心 csssl 承担，
 * 本类只做两件事：向 csssl 申请盖章、把 csssl 回执的令牌落本地台账。
 * HIS 侧不再自建 TSA（不生成时间戳密钥、不自行盖章），避免出现第二个信任根。
 */
@Slf4j
@Service
public class TsaChannelServiceImpl implements TsaChannelService {
    /**
     * 令牌规范化串版本前缀：与 csssl 的 TsaService.canonical 逐字一致 —— 两边必须同步改，否则历史令牌验不过
     */
    private static final String CANONICAL_PREFIX = "CSSSL-TSA-V1";

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BizTsaTokenMapper tokenMapper;
    private final CssslCaClient cssslCaClient;
    private final SignProperties signProperties;

    /**
     * csssl TSA 公钥缓存（验签用；csssl 不可达时无法验签，令牌校验一律按失败处理）
     */
    private volatile String cachedTsaPublicPem;

    public TsaChannelServiceImpl(BizTsaTokenMapper tokenMapper,
                                 CssslCaClient cssslCaClient,
                                 SignProperties signProperties) {
        this.tokenMapper = tokenMapper;
        this.cssslCaClient = cssslCaClient;
        this.signProperties = signProperties;
        log.info("[TSA通道] 初始化：统一走 csssl 认证中心 TSA，caBaseUrl={}", signProperties.getCaBaseUrl());
    }

    /** 外部（csssl）令牌规范化串——与 csssl 的 TsaService.canonical 同一实现，两处必须逐字一致 */
    private static String canonical(String serial, String digestHex, LocalDateTime tsaTime) {
        return CANONICAL_PREFIX + "|" + serial + "|" + digestHex.toLowerCase()
                + "|" + ISO.format(tsaTime);
    }

    /**
     * 通道名称（页面展示）
     */
    public String name() {
        return "CSSSL院内TSA（国密时间戳）";
    }

    public boolean available() {
        try {
            tsaPublicPem();
            return true;
        } catch (Exception e) {
            log.warn("csssl TSA 未就绪（{}）——签名时间来源将按「本机时钟」处理", e.getMessage());
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
     * TSA 公钥指纹（每 4 位分组）：取自 csssl TSA 公钥接口，与认证中心侧同一把密钥
     */
    public String fingerprintGroups() {
        try {
            Map<String, String> info = cssslCaClient.tsaPublicKey();
            String fingerprint = info.get("fingerprint");
            return SignCryptoUtil.fingerprintGroups(fingerprint);
        } catch (Exception e) {
            log.warn("获取 csssl TSA 公钥指纹失败：{}", e.getMessage());
            return null;
        }
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
            return externalStamp(digestHex);
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
            return externalVerify(serial, digestHex, tsaTime, tokenValue);
        } catch (Exception e) {
            log.warn("TSA令牌校验异常：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 失效内部缓存（运维接口启停/配置变更后调用，下次读取重新获取，操作即时生效）
     */
    public void invalidate() {
        cachedTsaPublicPem = null;
    }

    // ---------- csssl（认证中心）TSA ----------

    /** 取 TSA 公钥（PEM），并缓存；csssl 不可达时抛异常 */
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
            log.warn("TSA令牌序列号冲突（csssl TSA）serial={}，本次签名降级本机时钟", remote.serial());
            return null;
        }
        log.info("TSA盖章成功（csssl）serial={} 时刻={} 算法={}", remote.serial(), tsaTime, row.getAlgo());
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
                canonical(serial, digestHex, TimeUtil.toSeconds(tsaTime)), tokenValue);
    }
}
