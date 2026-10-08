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
     * 主档保存（建档 / 修改）之后的健康档案同步 —— 把「文本字段」与「结构化明细」按同一条规则对齐。
     *
     * <p>规则只有一条，两支互斥：
     * <ul>
     *   <li>该组<b>还没有明细行</b> → 主档文本是「快速录入的内容」，落成一条明细
     *       （既往病史一句「高血压病史5年」没有拆解规则，整句作为疾病名落一行，
     *       不猜诊断编码、不拆日期）；</li>
     *   <li>该组<b>已有明细行</b> → 明细是权威，主档文本按明细重算（覆盖手工改的文本）。
     *       主档那个输入框从此只是明细的投影视图，不再充当第二个存储。</li>
     * </ul>
     *
     * <p>为什么修改也要跑一遍：主档表单里至今还留着「过敏史」「既往病史」两个文本框
     * （{@code PatientsView} 的新建/编辑对话框）。只要它们还能被编辑，就必须有人在保存时
     * 把它翻译成明细 —— 否则用户在主档里改了过敏史、明细纹丝不动，两处又各说各话。
     */
    void syncAfterPatientSave(BizPatient patient);
}
