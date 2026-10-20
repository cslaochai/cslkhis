package com.his.patient.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.NoticeStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.support.CanonicalText;
import com.his.common.util.TimeUtil;
import com.his.patient.entity.BizCriticalNotice;
import com.his.patient.mapper.BizCriticalNoticeMapper;
import com.his.system.service.SignableContentService;
import com.his.spi.vo.SignSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 病危重通知的签名内容提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticalNoticeSignContentServiceImpl extends ServiceImpl<BizCriticalNoticeMapper, BizCriticalNotice> implements SignableContentService {

    private final BizCriticalNoticeMapper bizCriticalNoticeMapper;

    private static String canonical(BizCriticalNotice n) {
        return CanonicalText.create("CRITICAL_NOTICE")
                .put("noticeNo", n.getNoticeNo())
                .put("admissionId", n.getAdmissionId())
                .put("admissionNo", n.getAdmissionNo())
                .put("patientId", n.getPatientId())
                .put("patientNo", n.getPatientNo())
                .put("patientName", n.getPatientName())
                .put("gender", n.getGender())
                .put("age", n.getAge())
                .put("deptId", n.getDeptId())
                .put("deptName", n.getDeptName())
                .put("wardName", n.getWardName())
                .put("bedNo", n.getBedNo())
                .put("noticeType", n.getNoticeType())
                .put("consciousnessStatus", n.getConsciousnessStatus())
                .put("clinicalDiagnosis", n.getClinicalDiagnosis())
                .put("conditionDesc", n.getConditionDesc())
                .put("warningMatters", n.getWarningMatters())
                .put("doctorMeasures", n.getDoctorMeasures())
                .put("notifyTime", n.getNotifyTime())
                .put("doctorId", n.getDoctorId())
                .put("doctorName", n.getDoctorName())
                .put("witnessDoctorId", n.getWitnessDoctorId())
                .put("witnessDoctorName", n.getWitnessDoctorName())
                .build();
    }

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.CRITICAL_NOTICE;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizCriticalNotice n = bizCriticalNoticeMapper.selectById(bizId);
        if (n == null) {
            return null;
        }
        return new SignSubject(
                n.getId(),
                n.getNoticeNo(),
                n.getPatientId(),
                n.getPatientName(),
                n.getDeptId(),
                n.getDeptName(),
                n.getNoticeStatus(),
                NoticeStatusEnum.getLabel(n.getNoticeStatus(), "—"),
                canonical(n));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        if (scene != SignSceneEnum.NOTICE_ISSUE && scene != SignSceneEnum.NOTICE_MAKEUP) {
            return "病危重通知只支持「告知签发签名/告知补签」场景，当前场景「" + scene.getLabel() + "」不适用";
        }
        BizCriticalNotice n = bizCriticalNoticeMapper.selectById(subject.bizId());
        if (n == null) {
            return "病危重通知单不存在或已被删除，无法签名";
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), n.getSignStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已签名（签名即锁定），不能重复签名；"
                    + "如需修改，请先在「签名中心」作废该签名（作废会留痕并解除锁定）";
        }
        if (Objects.equals(NoticeStatusEnum.VOIDED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已作废，不能签名";
        }
        if (Objects.equals(NoticeStatusEnum.ACKED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已家属签收，内容冻结，不再接受签名";
        }
        if (scene == SignSceneEnum.NOTICE_ISSUE && Objects.equals(NoticeStatusEnum.ISSUED.getCode(), n.getNoticeStatus())) {
            return "通知单 " + n.getNoticeNo() + " 已签发（签名应已作废后重签），签发动作只做一次";
        }
        return null;
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizCriticalNotice patch = new BizCriticalNotice();
        patch.setId(bizId);
        patch.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
        patch.setSignId(signId);
        patch.setSignedTime(signedTime == null ? null : TimeUtil.toSeconds(signedTime));
        bizCriticalNoticeMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizCriticalNotice n = bizCriticalNoticeMapper.selectById(bizId);
        if (n == null || !Objects.equals(signId, n.getSignId())) {
            return;
        }
        bizCriticalNoticeMapper.update(null, new LambdaUpdateWrapper<BizCriticalNotice>()
                .eq(BizCriticalNotice::getId, bizId)
                .set(BizCriticalNotice::getSignStatus, ObjectSignStatusEnum.INVALIDATED.getCode())
                .set(BizCriticalNotice::getSignId, null));
        log.info("病危重通知签名已作废回写 noticeNo={} signId={}", n.getNoticeNo(), signId);
    }

}
