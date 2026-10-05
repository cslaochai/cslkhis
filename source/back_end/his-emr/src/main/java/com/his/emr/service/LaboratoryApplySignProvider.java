package com.his.emr.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.emr.entity.BizLaboratoryApply;

import java.time.LocalDateTime;

public interface LaboratoryApplySignProvider extends SignableContentProvider {

    static String statusText(Integer status) {
        return status == null ? "—" : ApplyStatusEnum.labelOrUnknown(status);
    }

    /**
     * 规范化文本：只含开单内容，按固定顺序
     */
    static String canonical(BizLaboratoryApply a) {
        return CanonicalText.create("LABORATORY_APPLY")
                .put("applyNo", a.getApplyNo())
                .put("patientId", a.getPatientId())
                .put("patientNo", a.getPatientNo())
                .put("patientName", a.getPatientName())
                .put("gender", a.getGender())
                .put("age", a.getAge())
                .put("registId", a.getRegistId())
                .put("visitDate", a.getVisitDate())
                .put("deptId", a.getDeptId())
                .put("deptName", a.getDeptName())
                .put("doctorId", a.getDoctorId())
                .put("doctorName", a.getDoctorName())
                .put("laboratoryItemId", a.getLaboratoryItemId())
                .put("laboratoryItemCode", a.getLaboratoryItemCode())
                .put("laboratoryItemName", a.getLaboratoryItemName())
                .put("laboratoryDeptId", a.getLaboratoryDeptId())
                .put("laboratoryDeptName", a.getLaboratoryDeptName())
                .put("specimenType", a.getSpecimenType())
                .put("laboratoryPurpose", a.getLaboratoryPurpose())
                .put("clinicalDiagnosis", a.getClinicalDiagnosis())
                .put("diseaseSummary", a.getDiseaseSummary())
                .put("isFasting", a.getIsFasting())
                .put("isEmergency", a.getIsEmergency())
                .put("price", plain(a.getPrice()))
                .build();
    }

    static String plain(java.math.BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }

    SignBizType bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignScene scene);

    void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}