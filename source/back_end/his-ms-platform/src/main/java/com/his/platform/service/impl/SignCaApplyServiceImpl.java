package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.CaApplyStatusEnum;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.platform.entity.SysSignCaApply;
import com.his.platform.entity.SysSignCert;
import com.his.platform.mapper.SysSignCaApplyMapper;
import com.his.platform.service.SignCaApplyService;
import com.his.platform.support.SignCaApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CA 申请流水服务实现：流水只增不删，状态与时刻就地回写当次那一行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignCaApplyServiceImpl extends ServiceImpl<SysSignCaApplyMapper, SysSignCaApply>
        implements SignCaApplyService {

    /**
     * 回执说明列宽
     */
    private static final int MESSAGE_MAX_LEN = 500;

    private final SysSignCaApplyMapper sysSignCaApplyMapper;

    @Override
    public SysSignCaApply start(SysSignCert cert, String externalRequestId, String templateCode, String subjectDn) {
        int seq = Math.toIntExact(sysSignCaApplyMapper.selectCount(new LambdaQueryWrapper<SysSignCaApply>()
                .eq(SysSignCaApply::getCertId, cert.getId()))) + 1;
        SysSignCaApply apply = new SysSignCaApply();
        apply.setCertId(cert.getId());
        apply.setCertNo(cert.getCertNo());
        apply.setApplySeq(seq);
        apply.setExternalRequestId(externalRequestId);
        apply.setTemplateCode(templateCode);
        apply.setSubjectDn(subjectDn);
        // 送单前先按待审核落库：CA 不可达时这条记录就是「什么时候试过」的唯一线索
        apply.setApplyStatus(CaApplyStatusEnum.PENDING.getCode());
        apply.setSubmittedAt(TimeUtil.nowSeconds());
        sysSignCaApplyMapper.insert(apply);
        log.info("登记 CA 申请 certNo={} 第{}次 请求号={}", cert.getCertNo(), seq, externalRequestId);
        return apply;
    }

    @Override
    public void recordCaResult(SysSignCaApply apply, SignCaApiClient.CaApplication app, String caMessage) {
        if (apply == null || app == null) {
            return;
        }
        if (TextUtil.hasText(app.applyNo())) {
            apply.setApplyNo(app.applyNo());
        }
        apply.setApplyStatus(app.status());
        apply.setCaMessage(TextUtil.cut(caMessage, MESSAGE_MAX_LEN));
        apply.setReviewedAt(app.reviewedAt());
        apply.setIssuedAt(app.issuedAt());
        sysSignCaApplyMapper.updateById(apply);
    }

    @Override
    public void recordFailure(SysSignCaApply apply, String errorMessage) {
        if (apply == null) {
            return;
        }
        apply.setApplyStatus(CaApplyStatusEnum.FAILED.getCode());
        apply.setCaMessage(TextUtil.cut(errorMessage, MESSAGE_MAX_LEN));
        sysSignCaApplyMapper.updateById(apply);
        log.warn("CA 申请失败留痕 certNo={} 第{}次 原因={}",
                apply.getCertNo(), apply.getApplySeq(), apply.getCaMessage());
    }

    @Override
    public void markBound(SysSignCaApply apply, String caIssuer) {
        if (apply == null) {
            return;
        }
        apply.setCaIssuer(caIssuer);
        apply.setBoundAt(TimeUtil.nowSeconds());
        sysSignCaApplyMapper.updateById(apply);
        log.info("CA 申请回填生效 certNo={} 第{}次 申请单={}",
                apply.getCertNo(), apply.getApplySeq(), apply.getApplyNo());
    }

    @Override
    public SysSignCaApply latestOf(Long certId) {
        return sysSignCaApplyMapper.selectOne(new LambdaQueryWrapper<SysSignCaApply>()
                .eq(SysSignCaApply::getCertId, certId)
                .orderByDesc(SysSignCaApply::getId)
                .last("LIMIT 1"));
    }

    @Override
    public SysSignCaApply findByApplyNoOrExternal(String applyNo, String externalRequestId) {
        if (TextUtil.hasText(applyNo)) {
            SysSignCaApply row = sysSignCaApplyMapper.selectOne(new LambdaQueryWrapper<SysSignCaApply>()
                    .eq(SysSignCaApply::getApplyNo, applyNo)
                    .orderByDesc(SysSignCaApply::getId)
                    .last("LIMIT 1"));
            if (row != null) {
                return row;
            }
        }
        if (!TextUtil.hasText(externalRequestId)) {
            return null;
        }
        return sysSignCaApplyMapper.selectOne(new LambdaQueryWrapper<SysSignCaApply>()
                .eq(SysSignCaApply::getExternalRequestId, externalRequestId)
                .orderByDesc(SysSignCaApply::getId)
                .last("LIMIT 1"));
    }

    @Override
    public void update(SysSignCaApply apply) {
        if (apply != null) {
            sysSignCaApplyMapper.updateById(apply);
        }
    }

    @Override
    public Map<Long, SysSignCaApply> latestOf(Collection<Long> certIds) {
        Map<Long, SysSignCaApply> result = new HashMap<>();
        if (certIds == null || certIds.isEmpty()) {
            return result;
        }
        // id 越大越新：按 id 倒序扫一遍，各证书第一次出现的那行就是最新一条
        List<SysSignCaApply> rows = sysSignCaApplyMapper.selectList(new LambdaQueryWrapper<SysSignCaApply>()
                .in(SysSignCaApply::getCertId, certIds)
                .orderByDesc(SysSignCaApply::getId));
        for (SysSignCaApply row : rows) {
            result.putIfAbsent(row.getCertId(), row);
        }
        return result;
    }

    @Override
    public void updateMessage(SysSignCaApply apply, String caMessage) {
        if (apply == null) {
            return;
        }
        apply.setCaMessage(TextUtil.cut(caMessage, MESSAGE_MAX_LEN));
        sysSignCaApplyMapper.updateById(apply);
    }

    @Override
    public List<SysSignCaApply> listByCertId(Long certId) {
        return sysSignCaApplyMapper.selectList(new LambdaQueryWrapper<SysSignCaApply>()
                .eq(SysSignCaApply::getCertId, certId)
                .orderByDesc(SysSignCaApply::getApplySeq)
                .orderByDesc(SysSignCaApply::getId));
    }
}
