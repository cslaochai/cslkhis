package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.TimeSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.platform.dto.TsaTokenPageQueryDTO;
import com.his.platform.entity.BizTsaToken;
import com.his.platform.mapper.BizTsaTokenMapper;
import com.his.platform.mapper.SignConfigMapper;
import com.his.platform.config.SignProperties;
import com.his.platform.service.RedisSequenceService;
import com.his.platform.service.TsaChannelService;
import com.his.platform.service.TsaService;
import com.his.platform.vo.TsaStatusVO;
import com.his.platform.vo.TsaTokenVO;
import com.his.platform.vo.TsaTokenVerifyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * TSA 运维查询实现（两个 GET，不产生任何写操作）。
 *
 * <p>TSA 服务本身由 csssl 认证中心承担（独立时间戳密钥、令牌签发、验签、审计），
 * 本类只负责 HIS 侧的令牌台账查询与时间来源配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TsaServiceImpl extends ServiceImpl<BizTsaTokenMapper, BizTsaToken> implements TsaService {

    private final TsaChannelService tsaChannelService;
    private final BizTsaTokenMapper bizTsaTokenMapper;
    private final SignConfigMapper signConfigMapper;
    private final SignProperties signProperties;
    private final RedisSequenceService redisSequenceService;

    private static Integer parseCfg(String v) {
        if (!TextUtil.hasText(v)) {
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
        boolean available = tsaChannelService.available();
        vo.setAvailable(available);

        String readyName = tsaChannelService.readyName();
        if (readyName != null) {
            vo.setTsaName(readyName);
        }
        // 信任根指纹取自 csssl TSA 公钥（与认证中心侧同一把密钥），供运维人工核对
        String fingerprintGroups = tsaChannelService.fingerprintGroups();
        if (fingerprintGroups != null) {
            vo.setKeyFingerprintGroups(fingerprintGroups);
        }

        Integer cfg = parseCfg(signConfigMapper.selectValue(SystemConfigKeyConst.SIGN_TIME_SOURCE));
        vo.setConfigTimeSource(cfg);
        int effective = effectiveTimeSource();
        vo.setEffectiveTimeSource(effective);
        vo.setEffectiveTimeSourceText(TimeSourceEnum.getLabel(effective));

        vo.setTokenCount(bizTsaTokenMapper.selectCount(null));
        BizTsaToken last = bizTsaTokenMapper.selectOne(new LambdaQueryWrapper<BizTsaToken>()
                .orderByDesc(BizTsaToken::getTsaTime)
                .orderByDesc(BizTsaToken::getId)
                .last("LIMIT 1"));
        vo.setLastTokenTime(last == null ? null : last.getTsaTime());

        if (available && effective == TimeSourceEnum.TSA.getCode()) {
            String name = vo.getTsaName() == null ? "TSA" : vo.getTsaName();
            vo.setTrustNote("已接入" + name
                    + "：令牌用 TSA 独立 SM2 密钥（SM3withSM2）签发，可对抗本机时钟篡改；"
                    + "信任源为院内 CA 认证中心（csssl）的 TSA 服务，令牌规范化串与盖章时刻以 csssl 回执为准。");
        } else if (cfg != null && cfg == TimeSourceEnum.TSA.getCode() && !available) {
            vo.setTrustNote("配置 sign.time_source=3 但 csssl TSA 服务不在线，实际生效的是「本机时钟」"
                    + "（宁可承认不可信，也不谎报可信）。请排查 csssl 服务状态与 his.sign.ca-base-url 配置。");
        } else if (available) {
            vo.setTrustNote("csssl TSA 服务在线，但 sign.time_source 未配为 3 —— 新签名时间仍取本机时钟，"
                    + "不具备对抗系统时间篡改的效力。接入方式：将 sys_config 的 sign.time_source 配为 3。");
        } else {
            vo.setTrustNote("未接入可信时间戳：签名时间取本机时钟，不具备对抗系统时间篡改的效力。"
                    + "接入方式：将 sys_config 的 sign.time_source 配为 3（需 csssl TSA 服务在线）。");
        }
        return vo;
    }

    @Override
    public IPage<TsaTokenVO> listPage(TsaTokenPageQueryDTO query) {
        LambdaQueryWrapper<BizTsaToken> w = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(query.getSerial())) {
            w.eq(BizTsaToken::getSerial, query.getSerial().trim());
        }
        if (TextUtil.hasText(query.getKeyword())) {
            w.like(BizTsaToken::getDigestHex, query.getKeyword().trim());
        }
        w.orderByDesc(BizTsaToken::getTsaTime).orderByDesc(BizTsaToken::getId);
        IPage<BizTsaToken> page = bizTsaTokenMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), w);
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

    @Override
    public TsaStatusVO updateTimeSource(Integer timeSource) {
        if (timeSource == null
                || (timeSource != TimeSourceEnum.LOCAL.getCode() && timeSource != TimeSourceEnum.TSA.getCode())) {
            // 2（院内授时）没有对应实现，配了也只会按本机时钟跑——拒绝比静默降级诚实
            throw new BusinessException("时间来源只允许 1（本机时钟）或 3（可信时间戳）；"
                    + "2（院内授时服务器）尚未接入实现，禁止配置");
        }
        String text = timeSource == TimeSourceEnum.TSA.getCode()
                ? "3（可信时间戳）"
                : "1（本机时钟）";
        if (signConfigMapper.updateValue(SystemConfigKeyConst.SIGN_TIME_SOURCE, String.valueOf(timeSource)) == 0) {
            try {
                signConfigMapper.insertValue(redisSequenceService.next("SYS_CONFIG"), SystemConfigKeyConst.SIGN_TIME_SOURCE,
                        String.valueOf(timeSource), "签名时间来源", "运维接口写入：" + text);
            } catch (DuplicateKeyException e) {
                signConfigMapper.updateValue(SystemConfigKeyConst.SIGN_TIME_SOURCE, String.valueOf(timeSource));
            }
        }
        log.info("签名时间来源切换为 {}（运维操作）", text);
        return status();
    }

    @Override
    public TsaTokenVerifyVO verifyToken(Long id) {
        BizTsaToken t = id == null ? null : bizTsaTokenMapper.selectById(id);
        TsaTokenVerifyVO vo = new TsaTokenVerifyVO();
        vo.setVerifyTime(TimeUtil.nowSeconds());
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
        boolean ok = tsaChannelService.verifyToken(t.getSerial(), t.getDigestHex(), t.getTsaTime(), t.getTokenValue());
        vo.setValid(ok);
        if (!ok) {
            vo.setFailReason("令牌验证不通过：签名值与「序列号+摘要+时刻」对不上，"
                    + "或摘要与台账不一致，或无法获取 csssl TSA 公钥");
        }
        return vo;
    }

    @Override
    public int effectiveTimeSource() {
        Integer cfg = parseCfg(signConfigMapper.selectValue(SystemConfigKeyConst.SIGN_TIME_SOURCE));
        TimeSourceEnum ts = TimeSourceEnum.fromCode(cfg != null ? cfg : signProperties.getTimeSource());
        if (ts == null) {
            ts = TimeSourceEnum.LOCAL;
        }
        if (ts == TimeSourceEnum.TSA) {
            // 适配器在线才允许写 3 —— 没有 TSA 实现就降级：写 3 等于给可随手修改的时间盖上"可信时间戳"的章
            if (tsaChannelService.available()) {
                return TimeSourceEnum.TSA.getCode();
            }
            log.warn("配置 sign.time_source=3（可信时间戳），但 csssl TSA 服务未接入，"
                    + "签名时间来源已降级为「本机时钟」——宁可承认不可信，也不谎报可信");
            return TimeSourceEnum.LOCAL.getCode();
        }
        if (ts == TimeSourceEnum.HOSPITAL_NTP) {
            log.warn("配置 sign.time_source=2（院内授时服务器），但当前未接入授时服务，已降级为「本机时钟」");
            return TimeSourceEnum.LOCAL.getCode();
        }
        return ts.getCode();
    }
}
