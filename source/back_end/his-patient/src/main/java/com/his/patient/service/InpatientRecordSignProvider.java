package com.his.patient.service;

import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.support.CanonicalText;
import com.his.common.util.NumUtil;
import com.his.patient.entity.BizInpatientRecord;
import com.his.system.entity.SignSubject;
import com.his.system.service.SignableContentProvider;
import com.his.system.utils.SignCryptoUtil;

import java.time.LocalDateTime;

public interface InpatientRecordSignProvider extends SignableContentProvider {

    /**
     * 规范化文本。**只有内容字段**，字段顺序固定。
     *
     * <p>病情体征用原始数值而不是 {@code vitalSignsText} 之类的展示串：
     * 展示串将来可能改格式（比如单位换个写法），一改就让所有历史签名验不过。
     */
    public static String canonical(BizInpatientRecord r) {
        return CanonicalText.create("INPATIENT_RECORD")
                .put("recordNo", r.getRecordNo())
                .put("admissionId", r.getAdmissionId())
                .put("patientId", r.getPatientId())
                .put("patientNo", r.getPatientNo())
                .put("recordType", r.getRecordType())
                .put("recordTitle", r.getRecordTitle())
                .put("recordTime", r.getRecordTime())
                .put("chiefComplaint", r.getChiefComplaint())
                .put("presentIllness", r.getPresentIllness())
                .put("pastHistory", r.getPastHistory())
                .put("personalHistory", r.getPersonalHistory())
                .put("familyHistory", r.getFamilyHistory())
                .put("allergyHistory", r.getAllergyHistory())
                .put("temperature", NumUtil.plain(r.getTemperature()))
                .put("pulse", r.getPulse())
                .put("respiration", r.getRespiration())
                .put("systolicPressure", r.getSystolicPressure())
                .put("diastolicPressure", r.getDiastolicPressure())
                .put("height", NumUtil.plain(r.getHeight()))
                .put("weight", NumUtil.plain(r.getWeight()))
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
                .put("diagnosisName", r.getDiagnosisName())
                .put("diagnosisCode", r.getDiagnosisCode())
                .put("treatmentPlan", r.getTreatmentPlan())
                .put("courseNote", r.getCourseNote())
                .put("doctorId", r.getDoctorId())
                .put("doctorName", r.getDoctorName())
                .build();
    }

    /**
     * 供外部（如签名详情页）按当前内容重算摘要用
     */
    public static String digestOf(BizInpatientRecord r) {
        return SignCryptoUtil.sha256Hex(canonical(r));
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
