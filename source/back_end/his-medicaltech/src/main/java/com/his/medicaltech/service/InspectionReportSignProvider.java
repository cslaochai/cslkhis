package com.his.medicaltech.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.common.util.NumUtil;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.enums.InsRecordStatusEnum;

import java.time.LocalDateTime;

public interface InspectionReportSignProvider extends SignableContentProvider {

    /**
     * 检查记录状态文案（签名规范化文本用，脏值必须暴露原码值）。
     *
     * <p>走 {@link InsRecordStatusEnum#labelOrUnknown(Integer)}：签名要能看出签的是哪个版本的码值，
     * 脏值不能悄悄变成「已登记」这类合法值 —— 签错一份报告的责任是落在这行文本上的。
     */
    public static String statusText(Integer status) {
        return status == null ? "—" : InsRecordStatusEnum.labelOrUnknown(status);
    }

    /**
     * 规范化文本：只含"检查报告内容"，按固定顺序
     */
    public static String canonical(BizInspectionRecord r) {
        return CanonicalText.create("INSPECTION_REPORT")
                .put("recordNo", r.getRecordNo())
                .put("applyId", r.getApplyId())
                .put("applyNo", r.getApplyNo())
                .put("patientId", r.getPatientId())
                .put("patientNo", r.getPatientNo())
                .put("visitDate", r.getVisitDate())
                .put("applyDeptId", r.getApplyDeptId())
                .put("applyDeptName", r.getApplyDeptName())
                .put("applyDoctorId", r.getApplyDoctorId())
                .put("applyDoctorName", r.getApplyDoctorName())
                .put("inspectionItemId", r.getInspectionItemId())
                .put("inspectionItemCode", r.getInspectionItemCode())
                .put("inspectionItemName", r.getInspectionItemName())
                .put("inspectionDeptId", r.getInspectionDeptId())
                .put("inspectionDeptName", r.getInspectionDeptName())
                .put("bodyPart", r.getBodyPart())
                .put("inspectionPurpose", r.getInspectionPurpose())
                .put("clinicalDiagnosis", r.getClinicalDiagnosis())
                .put("price", NumUtil.plain(r.getPrice()))
                .put("resultDescription", r.getResultDescription())
                .put("resultConclusion", r.getResultConclusion())
                .build();
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}