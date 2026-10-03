package com.his.patient.service;

import com.his.patient.dto.PatientAllergyUpsertDTO;
import com.his.patient.dto.PatientContactUpsertDTO;
import com.his.patient.dto.PatientFamilyHistoryUpsertDTO;
import com.his.patient.dto.PatientMedicationHistoryUpsertDTO;
import com.his.patient.dto.PatientPastDiseaseUpsertDTO;
import com.his.patient.dto.PatientSurgeryHistoryUpsertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.vo.PatientAllergyVO;
import com.his.patient.vo.PatientContactVO;
import com.his.patient.vo.PatientFamilyHistoryVO;
import com.his.patient.vo.PatientMedicationHistoryVO;
import com.his.patient.vo.PatientPastDiseaseVO;
import com.his.patient.vo.PatientSurgeryHistoryVO;
import com.his.patient.vo.PatientHealthProfileVO;

/**
 * 患者健康档案（六组）业务闭环 —— 唯一的写入口。
 *
 * <p><b>为什么要有这一层。</b>改造前这六组是五个 Controller 各自直接调 Mapper 写库：
 * 没有校验（表列 {@code allergy_symptoms NOT NULL}，传 null 就 500）、
 * 不写创建人（不知道是谁录的）、写坏了没人管。
 * 更关键的是 —— 同一份信息在库里存了两份：结构化表
 * （药物过敏史等）和主档自由文本
 * （患者基本信息.allergy_history / medical_history / contact_*），
 * 而**改动任何一份都不会同步另一份**，于是页面上出现「过敏史（自述）写着青霉素、
 * 健康档案卡片写着暂无」这种自相矛盾。本服务把「写」全部收口，
 * 并在每次写之后重算主档的文本投影，两份存储从此不会各走各的。
 *
 * <p><b>权威口径（写死在这里，别在页面另立一套）：</b>
 * <ul>
 *   <li>结构化表 = <b>权威</b>（可增删改、带严重程度/日期/剂量这些字段）；</li>
 *   <li>主档 {@code allergy_history} / {@code medical_history} / {@code contact_*} = <b>投影</b>，
 *       由 {@code syncXxxProjection} 按结构化明细重算，前端不要直接改它；</li>
 *   <li>投影规则：该组明细有行 → 拼摘要写回；明细被删空 → 文本置空。
 *       之所以「删空也置空」，是因为留着旧文本会让过敏警示继续亮着，
 *       而明细里已经什么都没有了 —— 用户以为删掉了、系统还在报，这比丢掉一句手写描述严重。</li>
 * </ul>
 *
 * <p><b>投影为什么必须按组同步、不能三组一起算：</b>存量库里
 * 「联系人文本有值、联系人明细为空」的档案有 23 份（过敏 14、既往 10）。
 * 如果改一条过敏史就把三组投影一起重算，那 23 份联系人文本会当场被清空 ——
 * 那不是同步，是拿同步当借口删数据。所以只有**被改动的那一组**参与重算。
 */
public interface PatientHealthProfileService {

    /** 一次带回六组明细 + 主档文本投影快照 + 分叉标记 */
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

    /** 按联系人ID查单条（出参带字典翻译好的 relationshipText） */
    PatientContactVO getContact(Long contactId);

    void deleteContact(Long id);

    /** 重算主档 {@code allergy_history}（按过敏明细） */
    void syncAllergyProjection(Long patientId);

    /** 重算主档 {@code medical_history}（按既往疾病明细） */
    void syncPastDiseaseProjection(Long patientId);

    /** 重算主档 {@code contact_name / contact_phone / contact_relation}（按联系人明细） */
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
