package com.his.patient.service;

import com.his.system.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.system.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.patient.entity.BizCriticalNotice;

import java.time.LocalDateTime;

public interface CriticalNoticeSignProvider extends SignableContentProvider {

    /**
     * 规范化文本：只含告知内容＋签收人法定要素，按固定顺序
     */
    public static String canonical(BizCriticalNotice n) {
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

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
