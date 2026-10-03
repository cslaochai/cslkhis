package com.his.medicaltech.service;

import com.his.common.service.SignableContentProvider;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.entity.SignSubject;
import com.his.common.support.CanonicalText;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import java.time.LocalDateTime;

import java.math.BigDecimal;

public interface InspectionReportSignProvider extends SignableContentProvider {

    /** 检查记录状态文案；**未知码值渲染成未知(n)，不回落成"已登记"这类合法值** */
    public static String statusText(Integer status) {
        InsRecordStatusEnum e = InsRecordStatusEnum.getByCode(status);
        if (e != null) {
            return e.getDesc();
        }
        return status == null ? "—" : "未知(" + status + ")";
    }

    /** 规范化文本：只含"检查报告内容"，按固定顺序 */
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
                .put("price", plain(r.getPrice()))
                .put("resultDescription", r.getResultDescription())
                .put("resultConclusion", r.getResultConclusion())
                .build();
    }

    SignBizType bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignScene scene);

    void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
    /** BigDecimal 去尾零：金额 {@code 10.00} 与 {@code 10.0} 必须算出同一个摘要 */
    static String plain(BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }
}