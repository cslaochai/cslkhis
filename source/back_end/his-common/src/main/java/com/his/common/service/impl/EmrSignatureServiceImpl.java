package com.his.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.config.SignProperties;
import com.his.common.dto.SignCommandDTO;
import com.his.common.dto.SignatureQueryPageDTO;
import com.his.common.entity.BizEmrSignature;
import com.his.common.entity.SignSubject;
import com.his.common.entity.SysSignCert;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.mapper.BizEmrSignatureMapper;
import com.his.common.mapper.SignConfigMapper;
import com.his.common.mapper.SysSignCertMapper;
import com.his.common.service.*;
import com.his.common.util.DateFormats;
import com.his.common.util.SignCryptoUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.vo.ObjectSignatureVO;
import com.his.common.vo.SignVerifyVO;
import com.his.common.vo.SignatureSummaryVO;
import com.his.common.vo.SignatureVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 电子签名服务实现（his-common 通用层）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrSignatureServiceImpl implements EmrSignatureService {

    private static final String SIGN_NO_PREFIX = "SIG";
    private static final String CFG_TIME_SOURCE = "sign.time_source";
    /**
     * 单号冲突重试次数（与质控单同口径）
     */
    private static final int MAX_RETRY = 3;

    private final BizEmrSignatureMapper bizEmrSignatureMapper;
    private final SysSignCertMapper sysSignCertMapper;
    private final SignCertService signCertService;
    private final SignatureStoreService signatureStoreService;
    private final SignProperties signProperties;
    private final SignConfigMapper signConfigMapper;
    private final RedisSequenceService redisSequenceService;
    private final TsaChannelService tsaChannelService;
    private final com.his.common.mapper.BizTsaTokenMapper tsaTokenMapper;
    private final ObjectProvider<SignableContentProvider> providers;
    private final ObjectProvider<SignCoverageProvider> coverageProviders;

    // 验签

    private static Integer intValue(String s) {
        if (!TextUtil.hasText(s)) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 支持 "yyyy-MM-dd" 与 "yyyy-MM-dd HH:mm:ss"；只给日期时，起始补 00:00:00、结束补 23:59:59
     */
    private static LocalDateTime parseTime(String s, boolean endOfDay) {
        if (!TextUtil.hasText(s)) {
            return null;
        }
        String v = s.trim();
        try {
            if (v.length() == 10) {
                LocalDate d = LocalDate.parse(v);
                return endOfDay ? TimeUtil.dayEnd(d) : TimeUtil.dayStart(d);
            }
            String norm = v.replace('T', ' ');
            if (norm.length() == 16) {
                norm = norm + ":00";
            }
            return LocalDateTime.parse(norm.replace(' ', 'T'));
        } catch (Exception e) {
            throw new BusinessException("时间格式不正确（应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）：" + s);
        }
    }

    private static String shortDigest(String digest) {
        return digest == null ? null : digest.substring(0, Math.min(16, digest.length()));
    }

    // 作废

    @Override
    public SignatureVO sign(SignCommandDTO cmd) {
        validateCommand(cmd);
        SignSceneEnum scene = SignSceneEnum.parse(cmd.getSignScene());
        if (scene == null) {
            throw new BusinessException("签名场景取值不合法：" + cmd.getSignScene());
        }
        SignBizTypeEnum bizType = SignBizTypeEnum.parse(cmd.getBizType());
        SignableContentProvider provider = providerOf(cmd.getBizType());

        SignSubject subject = provider.load(cmd.getBizId());
        if (subject == null) {
            throw new BusinessException("签名对象不存在（" + bizType.getText() + " id=" + cmd.getBizId() + "）");
        }
        String block = provider.blockReason(subject, scene);
        if (TextUtil.hasText(block)) {
            throw new BusinessException(block);
        }

        SysSignCert cert = signCertService.ensureActiveCert(cmd.getSignerId(), cmd.getSignerName(),
                cmd.getSignerDeptId(), cmd.getSignerDeptName());
        String privatePem = signCertService.privatePemOf(cert);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            BizEmrSignature entity = buildSignature(cmd, scene, subject, cert, privatePem);
            try {
                signatureStoreService.insertAndAnchor(entity, provider, scene, cert);
                log.info("签名成功 signNo={} {} id={} 场景={} 签名人={} 证书={} 摘要={}",
                        entity.getSignNo(), bizType.getText(), subject.bizId(), scene.getText(),
                        entity.getSignerName(), entity.getCertNo(), shortDigest(entity.getContentDigest()));
                return toVO(entity, true);
            } catch (DuplicateKeyException e) {
                if (attempt == MAX_RETRY) {
                    throw new BusinessException("签名流水号连续冲突 " + MAX_RETRY + " 次（"
                            + entity.getSignNo() + "），请稍后重试");
                }
                log.warn("签名流水号冲突，重试第 {} 次：{}", attempt, entity.getSignNo());
            }
        }
        throw new BusinessException("签名失败");
    }

    // 查询
    private BizEmrSignature buildSignature(SignCommandDTO cmd, SignSceneEnum scene, SignSubject subject,
                                           SysSignCert cert, String privatePem) {
        BizEmrSignature prev = bizEmrSignatureMapper.selectLastByBiz(cmd.getBizType(), cmd.getBizId());
        String prevDigest = prev == null ? null : prev.getContentDigest();

        String content = subject.contentWithPrev(prevDigest);
        String digest = SignCryptoUtil.sha256Hex(content);
        String signValue = SignCryptoUtil.sign(privatePem, content);
        LocalDateTime now = TimeUtil.nowSeconds();

        BizEmrSignature e = new BizEmrSignature();
        e.setSignNo(nextSignNo());
        e.setBizType(cmd.getBizType());
        e.setBizId(cmd.getBizId());
        e.setBizNo(subject.bizNo());
        e.setPatientId(subject.patientId());
        e.setPatientName(subject.patientName());
        e.setDeptId(subject.deptId());
        e.setDeptName(subject.deptName());
        e.setSignScene(scene.getCode());
        e.setChainNo(bizEmrSignatureMapper.countByBiz(cmd.getBizType(), cmd.getBizId()) + 1);
        e.setPrevSignId(prev == null ? null : prev.getId());
        e.setPrevDigest(prevDigest);
        e.setSignerId(cmd.getSignerId());
        e.setSignerName(TextUtil.hasText(cmd.getSignerName()) ? cmd.getSignerName() : String.valueOf(cmd.getSignerId()));
        e.setSignerDeptId(cmd.getSignerDeptId());
        e.setSignerDeptName(cmd.getSignerDeptName());
        e.setSignerTitle(cmd.getSignerTitle());
        e.setCertId(cert.getId());
        e.setCertNo(cert.getCertNo());
        e.setDigestAlgo(SignCryptoUtil.DIGEST_ALGO);
        e.setSignAlgo(SignCryptoUtil.SIGN_ALGO);
        e.setContentDigest(digest);
        e.setSignValue(signValue);
        e.setContentSnapshot(content);
        e.setSignedTime(now);

        // 可信时间戳（G6）：配置了 TSA 且适配器在线，就在签名时刻之后盖一枚令牌。
        // 盖章失败（网络/服务异常）不允许签名失败：TSA 通道已收敛成 null，这里降级本机时钟。
        int effectiveTs = effectiveTimeSource();
        TsaChannelService.Stamp stamp = effectiveTs == TimeSourceEnum.TSA.getCode() ? tsaChannelService.stamp(digest) : null;
        if (stamp != null) {
            e.setTimeSource(TimeSourceEnum.TSA.getCode());
            e.setTsaSerial(stamp.serial());
            e.setTsaTime(stamp.tsaTime());
            e.setTsaToken(stamp.tokenValue());
        } else {
            e.setTimeSource(effectiveTs);
            e.setTsaSerial(null);
            e.setTsaTime(null);
            e.setTsaToken(null);
        }
        e.setSignStatus(SignStatusEnum.VALID.getCode());
        e.setVerifyStatus(SignVerifyStatusEnum.UNCHECKED.getCode());
        e.setVerifyCount(0);
        e.setClientIp(cmd.getClientIp());
        e.setRemark(cmd.getRemark());
        return e;
    }

    @Override
    public SignVerifyVO verify(Long signId) {
        // C-非 web 入参：EmrSignatureService.verify 是能力层对外 API，标量形参 Long 无 DTO 承载注解（现由 EmrSignatureController 转调，其他模块 service 可直接调），Bean Validation 不覆盖，保留
        if (signId == null) {
            throw new BusinessException("签名ID不能为空");
        }
        BizEmrSignature sig = bizEmrSignatureMapper.selectById(signId);
        if (sig == null) {
            throw new BusinessException("签名记录不存在");
        }
        return doVerify(sig);
    }

    private SignVerifyVO doVerify(BizEmrSignature sig) {
        SignVerifyVO vo = new SignVerifyVO();
        vo.setSignId(sig.getId());
        vo.setSignNo(sig.getSignNo());
        vo.setBizType(sig.getBizType());
        vo.setBizTypeText(SignBizTypeEnum.textOf(sig.getBizType()));
        vo.setBizId(sig.getBizId());
        vo.setBizNo(sig.getBizNo());
        vo.setPatientName(sig.getPatientName());
        vo.setSignScene(sig.getSignScene());
        vo.setSignSceneText(SignSceneEnum.textOf(sig.getSignScene()));
        vo.setChainNo(sig.getChainNo());
        vo.setSignerName(sig.getSignerName());
        vo.setSignerDeptName(sig.getSignerDeptName());
        vo.setCertNo(sig.getCertNo());
        vo.setSignedTime(sig.getSignedTime());
        vo.setTimeSource(sig.getTimeSource());
        vo.setTimeSourceText(TimeSourceEnum.textOf(sig.getTimeSource()));
        vo.setSignStatus(sig.getSignStatus());
        vo.setSignStatusText(SignStatusEnum.textOf(sig.getSignStatus()));
        vo.setDigestAtSign(sig.getContentDigest());
        vo.setCheckedAt(TimeUtil.nowSeconds());

        // 断言一：签名值本身（用签名时留存的内容快照，不用当前内容）
        SysSignCert cert = sig.getCertId() == null ? null : sysSignCertMapper.selectById(sig.getCertId());
        boolean signatureValid;
        String signFailReason = null;
        if (cert == null) {
            signatureValid = false;
            signFailReason = "签名所用证书（certId=" + sig.getCertId() + "）不存在，无法校验签名值";
        } else if (!TextUtil.hasText(sig.getContentSnapshot())) {
            signatureValid = false;
            signFailReason = "该签名未留存被签内容快照（历史数据），无法校验签名值";
        } else {
            try {
                signatureValid = SignCryptoUtil.verify(cert.getPublicKey(), sig.getContentSnapshot(), sig.getSignValue());
            } catch (SignCryptoUtil.SignException e) {
                signatureValid = false;
                signFailReason = "验签执行异常：" + e.getMessage();
            }
        }
        vo.setSignatureValid(signatureValid);

        // 断言二：当前业务内容是否仍与签名时一致
        boolean contentMatched = false;
        String digestNow = null;
        String objectMissing = null;
        SignableContentProvider provider = findProvider(sig.getBizType());
        if (provider == null) {
            objectMissing = "签名对象类型「" + SignBizTypeEnum.textOf(sig.getBizType())
                    + "」在当前部署里没有内容提供者（可能是模块未加载），无法比对内容";
        } else {
            SignSubject now = provider.load(sig.getBizId());
            if (now == null) {
                objectMissing = "签名对象已不存在（可能已被删除），无法比对内容";
            } else {
                digestNow = now.digestWithPrev(sig.getPrevDigest());
                contentMatched = digestNow.equals(sig.getContentDigest());
            }
        }
        vo.setContentMatched(contentMatched);
        vo.setDigestNow(digestNow);

        // 断言三（仅 time_source=3 的行）：可信时间戳令牌
        // 前两断言回答"签名证据是否完好、内容是否被改"；这一条回答"签名时刻是否可信"。
        // 令牌校验失败不推翻签名本身，但「可信时间」的声称当场不成立 —— 结论级别至少警告。
        Boolean tsaValid = null;
        String tsaNote = null;
        if (Objects.equals(TimeSourceEnum.TSA.getCode(), sig.getTimeSource())) {
            boolean hasToken = TextUtil.hasText(sig.getTsaSerial())
                    && sig.getTsaTime() != null
                    && TextUtil.hasText(sig.getTsaToken());
            if (!hasToken) {
                tsaValid = false;
                tsaNote = "签名记录标记为 TSA 时间来源，但缺少序列号/授时时刻/令牌（写入侧校验被绕过或数据被删改）";
            } else {
                try {
                    tsaValid = tsaChannelService.verifyToken(sig.getTsaSerial(), sig.getContentDigest(),
                            sig.getTsaTime(), sig.getTsaToken());
                } catch (Exception ex) {
                    tsaValid = false;
                    tsaNote = "时间戳令牌校验执行异常：" + ex.getMessage();
                }
                if (!Boolean.TRUE.equals(tsaValid) && tsaNote == null) {
                    tsaNote = "时间戳令牌校验失败：令牌与摘要/时刻不一致、令牌被替换，或台账缺失";
                }
            }
        }
        vo.setTsaValid(tsaValid);
        vo.setTsaSerial(sig.getTsaSerial());
        vo.setTsaTime(sig.getTsaTime());
        vo.setTsaNote(tsaNote);

        // 结论
        boolean invalidated = Objects.equals(SignStatusEnum.INVALID.getCode(), sig.getSignStatus());
        int level;
        String conclusion;
        if (!signatureValid) {
            level = 3;
            conclusion = "签名值校验失败：" + (signFailReason != null ? signFailReason
                    : "用证书 " + sig.getCertNo() + " 的公钥无法验通该签名值（签名值或公钥被替换）");
        } else if (objectMissing != null) {
            level = 2;
            conclusion = "签名值有效，但" + objectMissing + "；内容一致性**无法判定**（不能算通过）";
        } else if (!contentMatched) {
            level = 2;
            conclusion = "签名值有效，但内容已变更：当前内容摘要与签名时不一致（签名后被修改过）";
        } else if (invalidated) {
            level = 1;
            conclusion = "签名值与内容均无误，但该签名已被作废（原因：" + sig.getInvalidReason() + "），不作为有效签名";
        } else {
            level = 1;
            conclusion = "验签通过：签名值有效，且内容自签名后未发生变更";
        }
        // 可信时间戳不成立：不推翻签名值与内容两个断言，但"时间可信"的声称必须降级为警告
        if (Boolean.FALSE.equals(tsaValid)) {
            level = Math.max(level, 2);
            conclusion = conclusion + "；且可信时间戳校验不通过（" + tsaNote + "）";
        }
        vo.setConclusion(conclusion);
        vo.setConclusionLevel(level);

        // 落地核查结果（两个断言同时成立才算"验签通过"）
        try {
            signatureStoreService.updateVerifyResult(sig.getId(),
                    signatureValid && contentMatched ? SignVerifyStatusEnum.PASSED.getCode()
                            : SignVerifyStatusEnum.FAILED.getCode(),
                    vo.getCheckedAt());
        } catch (Exception ex) {
            // 核查结果写不进去不能吞掉——否则页面刚显示"验签失败"，列表里那条却还是"未校验"
            log.error("验签结果回写失败 signNo={}：{}", sig.getSignNo(), ex.getMessage());
            throw new BusinessException("验签已完成但结果回写失败：" + ex.getMessage()
                    + "（请重试；若持续失败请检查 biz_emr_signature 是否可写）");
        }
        return vo;
    }

    @Override
    public List<SignVerifyVO> verifyByBiz(Integer bizType, Long bizId) {
        // C-非 web 入参：标量形参的跨模块能力 API（EmrSignatureController 转调，其他 service 可直接调），不经 HTTP 参数绑定，Bean Validation 不覆盖，保留
        if (bizType == null || bizId == null) {
            throw new BusinessException("签名对象类型与对象ID不能为空");
        }
        List<SignVerifyVO> list = new ArrayList<>();
        for (BizEmrSignature sig : signaturesOf(bizType, bizId)) {
            list.add(doVerify(sig));
        }
        return list;
    }

    @Override
    public SignatureVO invalidate(Long signId, String reason, Long operatorId, String operatorName) {
        // C-非 web 入参：SignatureCenterServiceImpl 转发 + InpatientOrderServiceImpl 等内部流程直接调用（标量与 reason 均非 HTTP 绑定），
        // Bean Validation 不覆盖，保留
        if (signId == null) {
            throw new BusinessException("签名ID不能为空");
        }
        if (!TextUtil.hasText(reason)) {
            throw new BusinessException("作废原因必填（作废会解除病历的内容锁定，必须写明依据）");
        }
        BizEmrSignature sig = bizEmrSignatureMapper.selectById(signId);
        if (sig == null) {
            throw new BusinessException("签名记录不存在");
        }
        if (Objects.equals(SignStatusEnum.INVALID.getCode(), sig.getSignStatus())) {
            throw new BusinessException("签名 " + sig.getSignNo() + " 已作废，不能重复作废");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        signatureStoreService.updateInvalidate(signId, reason, now, operatorId, operatorName);

        SignableContentProvider provider = findProvider(sig.getBizType());
        if (provider != null) {
            try {
                provider.revokeSignAnchor(sig.getBizId(), signId);
            } catch (Exception e) {
                // 锚点回写失败必须暴露：否则病历还显示"已签名"，但签名已经作废了
                log.error("签名作废后回写业务锚点失败 signNo={}：{}", sig.getSignNo(), e.getMessage());
                throw new BusinessException("签名已作废，但回写业务锚点失败：" + e.getMessage()
                        + "（该对象的签名状态可能与实际不符，请立即排查）");
            }
        }
        log.info("作废签名 signNo={} 对象={}:{} 原因={} 操作人={}",
                sig.getSignNo(), SignBizTypeEnum.textOf(sig.getBizType()), sig.getBizId(), reason, operatorName);

        BizEmrSignature after = bizEmrSignatureMapper.selectById(signId);
        return toVO(after, true);
    }

    @Override
    public SignatureVO getById(Long id) {
        BizEmrSignature sig = bizEmrSignatureMapper.selectById(id);
        if (sig == null) {
            throw new BusinessException("签名记录不存在");
        }
        return toVO(sig, true);
    }

    // 私有辅助

    @Override
    public IPage<SignatureVO> listPage(SignatureQueryPageDTO q) {
        LambdaQueryWrapper<BizEmrSignature> w = new LambdaQueryWrapper<>();
        w.eq(q.getBizType() != null, BizEmrSignature::getBizType, q.getBizType());
        w.eq(q.getBizId() != null, BizEmrSignature::getBizId, q.getBizId());
        w.like(TextUtil.hasText(q.getBizNo()), BizEmrSignature::getBizNo, q.getBizNo());
        w.eq(q.getSignerId() != null, BizEmrSignature::getSignerId, q.getSignerId());
        w.eq(q.getSignScene() != null, BizEmrSignature::getSignScene, q.getSignScene());
        w.eq(q.getSignStatus() != null, BizEmrSignature::getSignStatus, q.getSignStatus());
        w.eq(q.getVerifyStatus() != null, BizEmrSignature::getVerifyStatus, q.getVerifyStatus());
        w.eq(q.getTimeSource() != null, BizEmrSignature::getTimeSource, q.getTimeSource());
        LocalDateTime begin = parseTime(q.getBeginTime(), false);
        LocalDateTime end = parseTime(q.getEndTime(), true);
        w.ge(begin != null, BizEmrSignature::getSignedTime, begin);
        w.le(end != null, BizEmrSignature::getSignedTime, end);
        if (TextUtil.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(BizEmrSignature::getSignerName, kw)
                    .or().like(BizEmrSignature::getPatientName, kw)
                    .or().like(BizEmrSignature::getBizNo, kw)
                    .or().like(BizEmrSignature::getSignNo, kw));
        }
        w.orderByDesc(BizEmrSignature::getSignedTime).orderByDesc(BizEmrSignature::getId);

        IPage<BizEmrSignature> page = bizEmrSignatureMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        Page<SignatureVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<SignatureVO> rows = new ArrayList<>(page.getRecords().size());
        for (BizEmrSignature s : page.getRecords()) {
            rows.add(toVO(s, false));
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public List<SignatureVO> listByBiz(Integer bizType, Long bizId) {
        List<SignatureVO> list = new ArrayList<>();
        for (BizEmrSignature s : signaturesOf(bizType, bizId)) {
            list.add(toVO(s, false));
        }
        return list;
    }

    @Override
    public ObjectSignatureVO objectStatus(Integer bizType, Long bizId) {
        SignBizTypeEnum t = SignBizTypeEnum.parse(bizType);
        if (t == null) {
            throw new BusinessException("未知的签名对象类型：" + bizType);
        }
        ObjectSignatureVO vo = new ObjectSignatureVO();
        vo.setBizType(bizType);
        vo.setBizTypeText(t.getText());
        vo.setBizId(bizId);

        SignableContentProvider provider = findProvider(bizType);
        if (provider != null) {
            SignSubject s = provider.load(bizId);
            if (s == null) {
                throw new BusinessException("签名对象不存在（" + t.getText() + " id=" + bizId + "）");
            }
            vo.setBizNo(s.bizNo());
            vo.setPatientName(s.patientName());
            vo.setBizStatus(s.bizStatus());
            vo.setBizStatusText(s.bizStatusText());
            vo.setCanSign(true);
            vo.setBlockReason(provider.blockReason(s, SignSceneEnum.MAKEUP));
            vo.setCanSign(!TextUtil.hasText(vo.getBlockReason()));
        } else {
            vo.setCanSign(false);
            vo.setBlockReason("该对象类型在当前部署里没有内容提供者，无法签名（仅能查看历史签名）");
        }

        BizEmrSignature current = bizEmrSignatureMapper.selectCurrentByBiz(bizType, bizId);
        if (current != null) {
            vo.setCurrentSignId(current.getId());
            vo.setCurrentSignNo(current.getSignNo());
            vo.setLastSignedTime(current.getSignedTime());
            vo.setObjectSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
            vo.setObjectSignStatusText(ObjectSignStatusEnum.SIGNED.getText());
        } else {
            BizEmrSignature last = bizEmrSignatureMapper.selectLastByBiz(bizType, bizId);
            if (last != null && Objects.equals(SignStatusEnum.INVALID.getCode(), last.getSignStatus())) {
                vo.setLastSignedTime(last.getSignedTime());
                vo.setObjectSignStatus(ObjectSignStatusEnum.INVALIDATED.getCode());
                vo.setObjectSignStatusText(ObjectSignStatusEnum.INVALIDATED.getText());
            } else {
                vo.setObjectSignStatus(ObjectSignStatusEnum.UNSIGNED.getCode());
                vo.setObjectSignStatusText(ObjectSignStatusEnum.UNSIGNED.getText());
            }
        }
        vo.setChain(listByBiz(bizType, bizId));
        return vo;
    }

    @Override
    public SignatureSummaryVO summary() {
        SignatureSummaryVO vo = new SignatureSummaryVO();

        List<BizEmrSignature> all = bizEmrSignatureMapper.selectList(new LambdaQueryWrapper<BizEmrSignature>()
                .select(BizEmrSignature::getId, BizEmrSignature::getSignStatus,
                        BizEmrSignature::getVerifyStatus, BizEmrSignature::getBizType,
                        BizEmrSignature::getSignedTime));
        long valid = 0;
        long invalid = 0;
        long unchecked = 0;
        long failed = 0;
        java.util.Set<Integer> types = new java.util.TreeSet<>();
        for (BizEmrSignature s : all) {
            boolean isValid = Objects.equals(SignStatusEnum.VALID.getCode(), s.getSignStatus());
            if (isValid) {
                valid++;
                if (Objects.equals(SignVerifyStatusEnum.UNCHECKED.getCode(), s.getVerifyStatus())) {
                    unchecked++;
                } else if (Objects.equals(SignVerifyStatusEnum.FAILED.getCode(), s.getVerifyStatus())) {
                    failed++;
                }
            } else {
                invalid++;
            }
            if (s.getBizType() != null) {
                types.add(s.getBizType());
            }
        }
        vo.setTotalSign((long) all.size());
        vo.setValidSign(valid);
        vo.setInvalidSign(invalid);
        vo.setVerifyUnchecked(unchecked);
        vo.setVerifyFailed(failed);
        vo.setTodaySign(bizEmrSignatureMapper.countSignedAfter(TimeUtil.dayStart(LocalDate.now())));
        vo.setLastSignTime(bizEmrSignatureMapper.selectLastSignedTime());
        vo.setBizTypeCount(types.size());
        StringBuilder tb = new StringBuilder();
        for (Integer t : types) {
            if (tb.length() > 0) {
                tb.append(" / ");
            }
            tb.append(SignBizTypeEnum.textOf(t));
        }
        vo.setBizTypeTexts(tb.length() == 0 ? "—" : tb.toString());

        vo.setCertTotal(certTotal());
        vo.setCertActive(signCertService.countByStatus(CertStatusEnum.ACTIVE.getCode()));
        vo.setCertRevoked(signCertService.countByStatus(CertStatusEnum.REVOKED.getCode()));
        vo.setCertAutoIssued(signCertService.countAutoIssued());
        vo.setCertEmployeeCount(signCertService.countActiveEmployees());

        // 覆盖率：按业务类型分别统计，分母为 0 时显示"—"（不显示 0%，也不显示 100%）
        List<SignatureSummaryVO.Coverage> coverages = new ArrayList<>();
        for (SignCoverageProvider p : coverageProviders) {
            SignCoverageProvider.SignCoverage c = p.coverage();
            SignatureSummaryVO.Coverage row = new SignatureSummaryVO.Coverage();
            row.setBizType(c.bizType());
            row.setBizTypeText(c.bizTypeText());
            row.setTotal(c.total());
            row.setSigned(c.signed());
            row.setPendingSign(c.pendingSign());
            row.setInvalidated(c.invalidated());
            row.setSignedRateText(c.total() <= 0 ? "—（该类型还没有可统计的对象）"
                    : String.format("%.1f%%", c.signed() * 100.0 / c.total()));
            coverages.add(row);
        }
        coverages.sort(Comparator.comparing(SignatureSummaryVO.Coverage::getBizType));
        vo.setCoverages(coverages);

        vo.setTsaAvailable(tsaChannelService.available());
        vo.setTsaName(tsaChannelService.readyName());
        vo.setTsaTokenCount(tsaTokenMapper.selectCount(null));

        TimeSourceEnum ts = TimeSourceEnum.parse(effectiveTimeSource());
        if (ts != null && ts == TimeSourceEnum.TSA) {
            vo.setTimeSourceNote("签名时间来源：可信时间戳（" + (vo.getTsaName() == null ? "TSA" : vo.getTsaName())
                    + " —— 本地内置信任根：令牌结构真实、可对抗本机时钟篡改；非外部 CA/TSA，不对外声称法律效力）");
        } else if (ts != null && ts.trusted()) {
            vo.setTimeSourceNote("签名时间来源：" + ts.getText());
        } else {
            vo.setTimeSourceNote("签名时间来源：本机时钟（**未接入第三方可信时间戳 TSA**，签名时刻不具备对抗系统时间篡改的效力）");
        }
        vo.setCertTrustNote("证书为院内生成并托管的 RSA-2048 证书（私钥经 PBKDF2+AES-GCM 加密存储），"
                + "属于电子签名；**不是**第三方 CA 签发的可靠电子签名，系统管理员在技术上具备代签能力。");
        List<String> notes = new ArrayList<>();
        notes.add("签名是留痕动作：同一对象可以有多次签名，历史签名永不删除，作废只追加作废信息。");
        notes.add("「未签名」与「签名已失效」是两种不同的事实，页面分开统计，不互相回落。");
        notes.add("存量文书（签名能力上线前归档）一律记为未签名，**不补签**——补签等于伪造证据。");
        vo.setNotes(notes);
        return vo;
    }

    @Override
    public int effectiveTimeSource() {
        Integer cfg = intValue(signConfigMapper.selectValue(CFG_TIME_SOURCE));
        TimeSourceEnum ts = TimeSourceEnum.parse(cfg != null ? cfg : signProperties.getTimeSource());
        if (ts == null) {
            ts = TimeSourceEnum.LOCAL;
        }
        if (ts == TimeSourceEnum.TSA) {
            // 适配器在线才允许写 3 —— 没有 TSA 实现就降级：写 3 等于给可随手修改的时间盖上"可信时间戳"的章
            if (tsaChannelService.available()) {
                return TimeSourceEnum.TSA.getCode();
            }
            log.warn("配置 sign.time_source=3（第三方 TSA），但当前部署未接入 TSA 服务，"
                    + "签名时间来源已降级为「本机时钟」——宁可承认不可信，也不谎报可信");
            return TimeSourceEnum.LOCAL.getCode();
        }
        if (ts == TimeSourceEnum.HOSPITAL_NTP) {
            log.warn("配置 sign.time_source=2（院内授时服务器），但当前未接入授时服务，已降级为「本机时钟」");
            return TimeSourceEnum.LOCAL.getCode();
        }
        return ts.getCode();
    }

    // C-非 web 入参：SignCommandDTO 由 EmrServiceImpl/PrescriptionServiceImpl/SignatureCenterServiceImpl(his-emr)、InpatientRecordServiceImpl/InpatientOrderServiceImpl/InpatientLeaveServiceImpl/CriticalNoticeServiceImpl(his-patient)、RadiologyReportServiceImpl/MedicalTechServiceImpl/EcgServiceImpl(his-medicaltech) 现场构造并直调，Bean Validation 不覆盖，保留
    private void validateCommand(SignCommandDTO cmd) {
        if (cmd == null) {
            throw new BusinessException("签名入参不能为空");
        }
        if (cmd.getBizType() == null) {
            throw new BusinessException("签名对象类型不能为空");
        }
        if (cmd.getBizId() == null) {
            throw new BusinessException("签名对象ID不能为空");
        }
        if (SignBizTypeEnum.parse(cmd.getBizType()) == null) {
            throw new BusinessException("未知的签名对象类型：" + cmd.getBizType());
        }
        if (cmd.getSignerId() == null) {
            throw new BusinessException("签名人不能为空（未取到当前登录用户的员工ID）");
        }
    }

    private List<BizEmrSignature> signaturesOf(Integer bizType, Long bizId) {
        return bizEmrSignatureMapper.selectList(new LambdaQueryWrapper<BizEmrSignature>()
                .eq(BizEmrSignature::getBizType, bizType)
                .eq(BizEmrSignature::getBizId, bizId)
                .orderByAsc(BizEmrSignature::getChainNo)
                .orderByAsc(BizEmrSignature::getId));
    }

    private SignableContentProvider providerOf(Integer bizType) {
        SignableContentProvider p = findProvider(bizType);
        if (p == null) {
            throw new BusinessException("签名对象类型「" + SignBizTypeEnum.textOf(bizType)
                    + "」尚未接入签名能力（未找到内容提供者）；请确认对应业务模块已加载");
        }
        return p;
    }

    private SignableContentProvider findProvider(Integer bizType) {
        SignBizTypeEnum t = SignBizTypeEnum.parse(bizType);
        if (t == null) {
            return null;
        }
        for (SignableContentProvider p : providers) {
            if (p.bizType() == t) {
                return p;
            }
        }
        return null;
    }

    private long certTotal() {
        return sysSignCertMapper.selectCount(null);
    }

    private String nextSignNo() {
        String prefix = SIGN_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE);
        // 优先 Redis 原子自增（永不撞）；Redis 不可用时回落"当天已用条数 +1"。
        // 回落分支在**同一业务事务内**撞唯一索引时没法靠重试解决
        // （事务已被标记 rollback-only，重试只会得到 "Transaction silently rolled back"），
        // 所以 Redis 是正常路径，count 只是"Redis 挂了也要能签名"的兜底。
        try {
            long seq = redisSequenceService.next("SIGN");
            return prefix + String.format("%06d", seq);
        } catch (Exception e) {
            log.warn("签名号取号失败，回落为计数方式（{}）", e.getMessage());
            return prefix + String.format("%06d", bizEmrSignatureMapper.countBySignNoPrefix(prefix) + 1);
        }
    }

    private SignatureVO toVO(BizEmrSignature s, boolean withSnapshot) {
        SignatureVO vo = new SignatureVO();
        vo.setId(s.getId());
        vo.setSignNo(s.getSignNo());
        vo.setBizType(s.getBizType());
        vo.setBizTypeText(SignBizTypeEnum.textOf(s.getBizType()));
        vo.setBizId(s.getBizId());
        vo.setBizNo(s.getBizNo());
        vo.setPatientId(s.getPatientId());
        vo.setPatientName(s.getPatientName());
        vo.setDeptName(s.getDeptName());
        vo.setSignScene(s.getSignScene());
        vo.setSignSceneText(SignSceneEnum.textOf(s.getSignScene()));
        vo.setChainNo(s.getChainNo());
        vo.setPrevSignId(s.getPrevSignId());
        vo.setSignerId(s.getSignerId());
        vo.setSignerName(s.getSignerName());
        vo.setSignerDeptName(s.getSignerDeptName());
        vo.setSignerTitle(s.getSignerTitle());
        vo.setCertId(s.getCertId());
        vo.setCertNo(s.getCertNo());
        vo.setDigestAlgo(s.getDigestAlgo());
        vo.setSignAlgo(s.getSignAlgo());
        vo.setContentDigest(s.getContentDigest());
        vo.setContentDigestShort(shortDigest(s.getContentDigest()));
        vo.setSignedTime(s.getSignedTime());
        vo.setTimeSource(s.getTimeSource());
        vo.setTimeSourceText(TimeSourceEnum.textOf(s.getTimeSource()));
        vo.setTsaSerial(s.getTsaSerial());
        vo.setTsaTime(s.getTsaTime());
        TimeSourceEnum ts = TimeSourceEnum.parse(s.getTimeSource());
        vo.setTimeSourceNote(ts != null && ts.trusted() ? null : "不具备可信时间戳效力（系统时间可由本机修改）");
        vo.setSignStatus(s.getSignStatus());
        vo.setSignStatusText(SignStatusEnum.textOf(s.getSignStatus()));
        vo.setVerifyStatus(s.getVerifyStatus());
        vo.setVerifyStatusText(SignVerifyStatusEnum.textOf(s.getVerifyStatus()));
        vo.setVerifyTime(s.getVerifyTime());
        vo.setVerifyCount(s.getVerifyCount());
        vo.setInvalidReason(s.getInvalidReason());
        vo.setInvalidTime(s.getInvalidTime());
        vo.setInvalidByName(s.getInvalidByName());
        vo.setHasSnapshot(TextUtil.hasText(s.getContentSnapshot()));
        if (withSnapshot) {
            vo.setContentSnapshot(s.getContentSnapshot());
        }
        boolean invalidated = Objects.equals(SignStatusEnum.INVALID.getCode(), s.getSignStatus());
        vo.setCanVerify(true);
        vo.setCanInvalidate(!invalidated);
        if (invalidated) {
            vo.setActionHint("签名已于 " + s.getInvalidTime() + " 被 " + s.getInvalidByName()
                    + " 作废（" + s.getInvalidReason() + "），不可重复作废；仍可验签查看证据是否完好");
        } else {
            vo.setActionHint(null);
        }
        return vo;
    }
}
