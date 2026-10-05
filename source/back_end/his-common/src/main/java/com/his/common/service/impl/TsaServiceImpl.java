package com.his.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.RedisSequenceService;
import com.his.common.dto.TsaTokenQueryPageDTO;
import com.his.common.entity.BizTsaToken;
import com.his.common.entity.SysTsaServer;
import com.his.common.enums.TimeSource;
import com.his.common.exception.BusinessException;
import com.his.common.mapper.BizTsaTokenMapper;
import com.his.common.mapper.SignConfigMapper;
import com.his.common.mapper.SysTsaServerMapper;
import com.his.common.service.EmrSignatureService;
import com.his.common.service.TsaChannelService;
import com.his.common.service.TsaService;
import com.his.common.util.SignCrypto;
import com.his.common.vo.TsaStatusVO;
import com.his.common.vo.TsaTokenVO;
import com.his.common.vo.TsaTokenVerifyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * TSA 运维查询实现（两个 GET，不产生任何写操作）。
 *
 * <p>状态接口读时间戳服务注册表只为展示公钥指纹，**不解密私钥、不触发自举**
 * —— 查个状态把密钥生成出来属于副作用，绝不干。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TsaServiceImpl implements TsaService {

    private static final String CFG_TIME_SOURCE = "sign.time_source";

    private final TsaChannelService tsaChannel;
    private final SysTsaServerMapper serverMapper;
    private final BizTsaTokenMapper tokenMapper;
    private final SignConfigMapper configMapper;
    private final EmrSignatureService signatureService;
    private final RedisSequenceService sequenceService;

    private static Integer parseCfg(String v) {
        if (!StringUtils.hasText(v)) {
            return null;
        }
        try {
            return Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public TsaStatusVO status() {
        TsaStatusVO vo = new TsaStatusVO();
        boolean available = tsaChannel.available();
        vo.setAvailable(available);

        String readyName = tsaChannel.readyName();
        if (readyName != null) {
            vo.setTsaName(readyName);
        }
        SysTsaServer server = serverMapper.selectOne(new LambdaQueryWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, "LOCAL"));
        if (server != null) {
            vo.setKeyFingerprintGroups(SignCrypto.fingerprintGroups(server.getKeyFingerprint()));
            if (vo.getTsaName() == null) {
                vo.setTsaName(server.getTsaName());
            }
        }

        Integer cfg = parseCfg(configMapper.selectValue(CFG_TIME_SOURCE));
        vo.setConfigTimeSource(cfg);
        int effective = signatureService.effectiveTimeSource();
        vo.setEffectiveTimeSource(effective);
        vo.setEffectiveTimeSourceText(TimeSource.textOf(effective));

        vo.setTokenCount(tokenMapper.selectCount(null));
        BizTsaToken last = tokenMapper.selectOne(new LambdaQueryWrapper<BizTsaToken>()
                .orderByDesc(BizTsaToken::getTsaTime)
                .orderByDesc(BizTsaToken::getId)
                .last("LIMIT 1"));
        vo.setLastTokenTime(last == null ? null : last.getTsaTime());

        // 配置了 3 却降级 = 最常见的误读点，必须在这里写明白；
        // 适配器在线但没启用 = 次常见（文案不能说"未接入"，与上面的「在线」同屏自相矛盾）
        if (available && effective == TimeSource.TSA.getCode()) {
            vo.setTrustNote("已接入" + (vo.getTsaName() == null ? "TSA" : vo.getTsaName())
                    + "：令牌用 TSA 独立密钥签发，可对抗本机时钟篡改。注意：信任根为院内（本地内置 TSA，"
                    + "非第三方 CA/TSA），不对外声称法律效力；换真 TSA 只需替换适配器实现。");
        } else if (cfg != null && cfg == TimeSource.TSA.getCode() && !available) {
            vo.setTrustNote("配置 sign.time_source=3 但 TSA 适配器不在线，实际生效的是「本机时钟」"
                    + "（宁可承认不可信，也不谎报可信）。请排查 sys_tsa_server 行状态与主口令配置。");
        } else if (available) {
            vo.setTrustNote("TSA 适配器在线，但 sign.time_source 未配为 3 —— 新签名时间仍取本机时钟，"
                    + "不具备对抗系统时间篡改的效力。接入方式：将 sys_config 的 sign.time_source 配为 3。");
        } else {
            vo.setTrustNote("未接入可信时间戳：签名时间取本机时钟，不具备对抗系统时间篡改的效力。"
                    + "接入方式：将 sys_config 的 sign.time_source 配为 3（需 TSA 适配器在线）。");
        }
        return vo;
    }

    @Override
    public IPage<TsaTokenVO> listPage(TsaTokenQueryPageDTO query) {
        LambdaQueryWrapper<BizTsaToken> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getSerial())) {
            w.eq(BizTsaToken::getSerial, query.getSerial().trim());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            w.like(BizTsaToken::getDigestHex, query.getKeyword().trim());
        }
        w.orderByDesc(BizTsaToken::getTsaTime).orderByDesc(BizTsaToken::getId);
        IPage<BizTsaToken> page = tokenMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), w);
        Page<TsaTokenVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TsaTokenVO> rows = new ArrayList<>(page.getRecords().size());
        for (BizTsaToken t : page.getRecords()) {
            TsaTokenVO vo = new TsaTokenVO();
            vo.setId(t.getId());
            vo.setSerial(t.getSerial());
            vo.setDigestHex(t.getDigestHex());
            vo.setTsaTime(t.getTsaTime());
            vo.setTokenValue(t.getTokenValue());
            vo.setAlgo(t.getAlgo());
            vo.setCreateTime(t.getCreateTime());
            vo.setRemark(t.getRemark());
            rows.add(vo);
        }
        result.setRecords(rows);
        return result;
    }

    // G6b 运维操作：启停 / 时间来源 / 令牌复验

    @Override
    public TsaStatusVO updateStatus(Integer tsaStatus) {
        if (tsaStatus == null || (tsaStatus != 0 && tsaStatus != 1)) {
            throw new BusinessException("目标状态只允许 0（停用）或 1（启用）");
        }
        int updated = serverMapper.update(null, new LambdaUpdateWrapper<SysTsaServer>()
                .eq(SysTsaServer::getTsaCode, "LOCAL")
                .set(SysTsaServer::getTsaStatus, tsaStatus));
        if (updated == 0) {
            // 行不存在时**绝不顺带创建**——自举是 TSA 通道的职责且需要主口令，
            // 这里造一行没有密钥的空壳只会把"未就绪"伪装成"已配置"
            throw new BusinessException("本地 TSA 服务行不存在（尚未自举），无法直接启停");
        }
        // 失效实现方缓存：available()/盖章下一次调用重读库，操作即时生效
        tsaChannel.invalidate();
        log.info("TSA服务{}（G6b 运维操作）", tsaStatus == 1 ? "启用" : "停用");
        return status();
    }

    @Override
    public TsaStatusVO updateTimeSource(Integer timeSource) {
        if (timeSource == null
                || (timeSource != TimeSource.LOCAL.getCode() && timeSource != TimeSource.TSA.getCode())) {
            // 2（院内授时）没有对应实现，配了也只会按本机时钟跑——拒绝比静默降级诚实
            throw new BusinessException("时间来源只允许 1（本机时钟）或 3（可信时间戳）；"
                    + "2（院内授时服务器）尚未接入实现，禁止配置");
        }
        String text = timeSource == TimeSource.TSA.getCode()
                ? "3（第三方可信时间戳，经本地内置TSA适配）"
                : "1（本机时钟）";
        // config_id 非自增：先更后插，插不进（并发新建撞唯一键）就再更一次兜底
        if (configMapper.updateValue(CFG_TIME_SOURCE, String.valueOf(timeSource)) == 0) {
            try {
                configMapper.insertValue(sequenceService.next("SYS_CONFIG"), CFG_TIME_SOURCE,
                        String.valueOf(timeSource), "签名时间来源", "G6b 运维接口写入：" + text);
            } catch (DuplicateKeyException e) {
                configMapper.updateValue(CFG_TIME_SOURCE, String.valueOf(timeSource));
            }
        }
        log.info("签名时间来源切换为 {}（G6b 运维操作）", text);
        return status();
    }

    @Override
    public TsaTokenVerifyVO verifyToken(Long id) {
        BizTsaToken t = id == null ? null : tokenMapper.selectById(id);
        TsaTokenVerifyVO vo = new TsaTokenVerifyVO();
        vo.setVerifyTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        if (t == null) {
            vo.setValid(false);
            vo.setFailReason("令牌不存在（台账中无此 ID）");
            return vo;
        }
        vo.setId(t.getId());
        vo.setSerial(t.getSerial());
        vo.setDigestHex(t.getDigestHex());
        vo.setTsaTime(t.getTsaTime());
        vo.setAlgo(t.getAlgo());
        boolean ok = tsaChannel.verifyToken(t.getSerial(), t.getDigestHex(), t.getTsaTime(), t.getTokenValue());
        vo.setValid(ok);
        if (!ok) {
            vo.setFailReason("令牌验证不通过：签名值与「序列号+摘要+时刻」对不上，"
                    + "或摘要与台账不一致，或 TSA 公钥未配置");
        }
        return vo;
    }
}
