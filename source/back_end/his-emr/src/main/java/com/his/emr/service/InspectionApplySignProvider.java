package com.his.emr.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.ApplyStatusEnum;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.emr.entity.BizInspectionApply;

import java.time.LocalDateTime;

public interface InspectionApplySignProvider extends SignableContentProvider {

    static String statusText(Integer status) {
        if (status == null) {
            return "—";
        }
        ApplyStatusEnum e = ApplyStatusEnum.fromCode(status);
        return e.getCode() == status ? e.getLabel() : "未知(" + status + ")";
    }

    /**
     * 规范化文本：只含开单内容，按固定顺序
     */
    static String canonical(BizInspectionApply a) {
        return CanonicalText.create("INSPECTION_APPLY")
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
                .put("inspectionItemId", a.getInspectionItemId())
                .put("inspectionItemCode", a.getInspectionItemCode())
                .put("inspectionItemName", a.getInspectionItemName())
                .put("inspectionDeptId", a.getInspectionDeptId())
                .put("inspectionDeptName", a.getInspectionDeptName())
                .put("bodyPart", a.getBodyPart())
                .put("inspectionPurpose", a.getInspectionPurpose())
                .put("clinicalDiagnosis", a.getClinicalDiagnosis())
                .put("diseaseSummary", a.getDiseaseSummary())
                .put("specialRequirements", a.getSpecialRequirements())
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