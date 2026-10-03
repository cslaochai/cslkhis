package com.his.medicaltech.service;

import com.his.common.service.SignableContentProvider;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.entity.SignSubject;
import com.his.common.support.CanonicalText;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.enums.LabRecordStatusEnum;
import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;


public interface LaboratoryReportSignProvider extends SignableContentProvider {

    /** 检验记录状态文案；**未知码值渲染成未知(n)，不回落成"已登记"这类合法值** */
    public static String statusText(Integer status) {
        if (status == null) {
            return "—";
        }
        LabRecordStatusEnum e = LabRecordStatusEnum.getByCode(status);
        return e == null ? "未知(" + status + ")" : e.getDescription();
    }

    /** 规范化文本：报告头 + **结果明细**，不含任何流程字段 */
    public static String canonical(BizLaboratoryRecord r, List<BizLabResult> results) {
        return CanonicalText.create("LAB_REPORT")
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
                .put("laboratoryItemId", r.getLaboratoryItemId())
                .put("laboratoryItemCode", r.getLaboratoryItemCode())
                .put("laboratoryItemName", r.getLaboratoryItemName())
                .put("laboratoryDeptId", r.getLaboratoryDeptId())
                .put("laboratoryDeptName", r.getLaboratoryDeptName())
                .put("specimenType", r.getSpecimenType())
                .put("specimenNo", r.getSpecimenNo())
                .put("price", plain(r.getPrice()))
                .put("diagnosis", r.getDiagnosis())
                .put("suggestions", r.getSuggestions())
                .put("results", resultsText(results))
                .build();
    }

    SignBizType bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignScene scene);

    void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
    /** 明细逐条一行，字段用 {@code |} 分隔、行间换行 */
    static String resultsText(List<BizLabResult> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (BizLabResult r : results) {
            i++;
            sb.append(i).append('|')
                    .append(nz(r.getLaboratoryItemCode())).append('|')
                    .append(nz(r.getLaboratoryItemName())).append('|')
                    .append(nz(r.getResultValue())).append('|')
                    .append(nz(r.getResultUnit())).append('|')
                    .append(nz(r.getReferenceRange())).append('|')
                    .append(r.getAbnormalFlag() == null ? "" : r.getAbnormalFlag())
                    .append('\n');
        }
        return sb.toString();
    }
    static String plain(BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }
    static String nz(String s) {
        return s == null ? "" : s;
    }
}