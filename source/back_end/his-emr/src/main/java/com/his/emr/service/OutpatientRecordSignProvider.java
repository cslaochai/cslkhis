package com.his.emr.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.emr.entity.BizMedicalRecord;

import java.time.LocalDateTime;

public interface OutpatientRecordSignProvider extends SignableContentProvider {

    /**
     * 门诊病历状态文案（与前端 1草稿/2已提交/3已归档/4已作废一致）；未知码值不回落
     */
    static String recordStatusText(Integer status) {
        return status == null ? "—" : RecordStatusEnum.labelOrUnknown(status);
    }

    /**
     * 规范化文本：只含病历内容字段，顺序固定
     */
    static String canonical(BizMedicalRecord r) {
        return CanonicalText.create("OUTPATIENT_RECORD")
                .put("recordNo", r.getRecordNo())
                .put("patientId", r.getPatientId())
                .put("patientNo", r.getPatientNo())
                .put("registId", r.getRegistId())
                .put("registNo", r.getRegistNo())
                .put("visitDate", r.getVisitDate())
                .put("visitType", r.getVisitType())
                .put("deptId", r.getDeptId())
                .put("doctorId", r.getDoctorId())
                .put("doctorName", r.getDoctorName())
                .put("chiefComplaint", r.getChiefComplaint())
                .put("presentIllness", r.getPresentIllness())
                .put("pastHistory", r.getPastHistory())
                .put("personalHistory", r.getPersonalHistory())
                .put("familyHistory", r.getFamilyHistory())
                .put("allergyHistory", r.getAllergyHistory())
                .put("temperature", r.getTemperature())
                .put("pulse", r.getPulse())
                .put("respiration", r.getRespiration())
                .put("systolicPressure", r.getSystolicPressure())
                .put("diastolicPressure", r.getDiastolicPressure())
                .put("generalCondition", r.getGeneralCondition())
                .put("skinMucosa", r.getSkinMucosa())
                .put("headNeck", r.getHeadNeck())
                .put("chestLung", r.getChestLung())
                .put("heart", r.getHeart())
                .put("abdomen", r.getAbdomen())
                .put("spineLimbs", r.getSpineLimbs())
                .put("nervousSystem", r.getNervousSystem())
                .put("specialistExam", r.getSpecialistExam())
                .put("auxiliaryExam", r.getAuxiliaryExam())
                .put("diagnosis", r.getDiagnosis())
                .put("diagnosisCode", r.getDiagnosisCode())
                .put("diagnosisName", r.getDiagnosisName())
                .put("treatmentPlan", r.getTreatmentPlan())
                .build();
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
