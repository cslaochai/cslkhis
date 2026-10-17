package com.his.patient.service;

import com.his.patient.dto.*;
import com.his.api.dto.*;
import com.his.patient.entity.BizPatient;
import com.his.patient.vo.*;
import com.his.api.vo.*;

/**
 * 患者健康档案（六组）业务闭环 —— 唯一的写入口。
 */
public interface PatientHealthProfileService {

    /**
     * 一次带回六组明细 + 主档文本投影快照 + 分叉标记
     */
    PatientHealthProfileVO getProfile(Long patientId);

    PatientAllergyVO saveAllergy(PatientAllergyUpsertDTO dto);

    void deleteAllergy(Long id);

    PatientPastDiseaseVO savePastDisease(PatientPastDiseaseUpsertDTO dto);

    void deletePastDisease(Long id);

    PatientSurgeryHistoryVO saveSurgeryHistory(PatientSurgeryHistoryUpsertDTO dto);

    void deleteSurgeryHistory(Long id);

    PatientFamilyHistoryVO saveFamilyHistory(PatientFamilyHistoryUpsertDTO dto);

    void deleteFamilyHistory(Long id);

    PatientMedicationHistoryVO saveMedication(PatientMedicationHistoryUpsertDTO dto);

    void deleteMedication(Long id);

    PatientContactVO saveContact(PatientContactUpsertDTO dto);

    /**
     * 按联系人ID查单条（出参带字典翻译好的 relationshipText）
     */
    PatientContactVO getContact(Long contactId);

    void deleteContact(Long id);

    /**
     * 重算主档 {@code allergy_history}（按过敏明细）
     */
    void syncAllergyProjection(Long patientId);

    /**
     * 重算主档 {@code medical_history}（按既往疾病明细）
     */
    void syncPastDiseaseProjection(Long patientId);

    /**
     * 重算主档 {@code contact_name / contact_phone / contact_relation}（按联系人明细）
     */
    void syncContactProjection(Long patientId);

    /**
     * 主档保存（建档 / 修改）
     */
    void syncAfterPatientSave(BizPatient patient);
}
