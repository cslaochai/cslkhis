package com.his.patient.service;

import com.his.patient.dto.*;
import com.his.patient.entity.BizPatient;
import com.his.patient.vo.*;

/**
 * 患者健康档案（六组）业务闭环 —— 唯一的写入口。
 */
public interface PatientHealthProfileService {

    /**
     * 一次带回六组明细 + 主档文本投影快照 + 分叉标记
     */
    PatientHealthProfileVO getProfile(Long patientId);

    /**
     * 保存过敏史一条（带 id 修改、不带 id 新增）：并同步主档过敏文本投影。
     * 用于「健康档案」编辑页过敏史组的明细行保存。
     */
    PatientAllergyVO saveAllergy(PatientAllergyUpsertDTO dto);

    /**
     * 删除过敏史一条：删后重算主档过敏文本投影。
     * 用于「健康档案」编辑页过敏史组明细行的删除。
     */
    void deleteAllergy(Long id);

    /**
     * 保存既往疾病一条（带 id 修改、不带 id 新增）：并同步主档疾病文本投影。
     * 用于「健康档案」编辑页既往疾病组的明细行保存。
     */
    PatientPastDiseaseVO savePastDisease(PatientPastDiseaseUpsertDTO dto);

    /**
     * 删除既往疾病一条：删后重算主档疾病文本投影。
     * 用于「健康档案」编辑页既往疾病组明细行的删除。
     */
    void deletePastDisease(Long id);

    /**
     * 保存手术史一条（带 id 修改、不带 id 新增）。
     * 用于「健康档案」编辑页手术史组的明细行保存。
     */
    PatientSurgeryHistoryVO saveSurgeryHistory(PatientSurgeryHistoryUpsertDTO dto);

    /**
     * 删除手术史一条。
     * 用于「健康档案」编辑页手术史组明细行的删除。
     */
    void deleteSurgeryHistory(Long id);

    /**
     * 保存家族史一条（带 id 修改、不带 id 新增）。
     * 用于「健康档案」编辑页家族史组的明细行保存。
     */
    PatientFamilyHistoryVO saveFamilyHistory(PatientFamilyHistoryUpsertDTO dto);

    /**
     * 删除家族史一条。
     * 用于「健康档案」编辑页家族史组明细行的删除。
     */
    void deleteFamilyHistory(Long id);

    /**
     * 保存用药史一条（带 id 修改、不带 id 新增）。
     * 用于「健康档案」编辑页用药史组的明细行保存。
     */
    PatientMedicationHistoryVO saveMedication(PatientMedicationHistoryUpsertDTO dto);

    /**
     * 删除用药史一条。
     * 用于「健康档案」编辑页用药史组明细行的删除。
     */
    void deleteMedication(Long id);

    /**
     * 保存联系人一条（带 id 修改、不带 id 新增）：并同步主档联系人文本投影。
     * 用于「健康档案」编辑页联系人组的明细行保存。
     */
    PatientContactVO saveContact(PatientContactUpsertDTO dto);

    /**
     * 按联系人ID查单条（出参带字典翻译好的 relationshipText）
     */
    PatientContactVO getContact(Long contactId);

    /**
     * 删除联系人一条：删后重算主档联系人文本投影。
     * 用于「健康档案」编辑页联系人组明细行的删除。
     */
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
