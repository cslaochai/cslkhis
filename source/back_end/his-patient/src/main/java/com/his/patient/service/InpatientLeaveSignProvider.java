package com.his.patient.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.patient.entity.BizInpatientLeave;

import java.time.LocalDateTime;

public interface InpatientLeaveSignProvider extends SignableContentProvider {

    /**
     * 规范化文本：只含申请内容＋患方承诺要素＋审批落款，按固定顺序（与打印承诺书对齐）
     */
    public static String canonical(BizInpatientLeave l) {
        return CanonicalText.create("INPATIENT_LEAVE")
                .put("leaveNo", l.getLeaveNo())
                .put("admissionId", l.getAdmissionId())
                .put("admissionNo", l.getAdmissionNo())
                .put("patientId", l.getPatientId())
                .put("patientNo", l.getPatientNo())
                .put("patientName", l.getPatientName())
                .put("gender", l.getGender())
                .put("age", l.getAge())
                .put("deptId", l.getDeptId())
                .put("deptName", l.getDeptName())
                .put("wardName", l.getWardName())
                .put("bedNo", l.getBedNo())
                .put("leaveType", l.getLeaveType())
                .put("reason", l.getReason())
                .put("destination", l.getDestination())
                .put("companionName", l.getCompanionName())
                .put("companionRelation", l.getCompanionRelation())
                .put("companionPhone", l.getCompanionPhone())
                .put("expectedLeaveTime", l.getExpectedLeaveTime())
                .put("expectedReturnTime", l.getExpectedReturnTime())
                .put("applyTime", l.getApplyTime())
                .put("applyBy", l.getApplyBy())
                .put("doctorId", l.getDoctorId())
                .put("doctorName", l.getDoctorName())
                .put("doctorAdvice", l.getDoctorAdvice())
                .build();
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
