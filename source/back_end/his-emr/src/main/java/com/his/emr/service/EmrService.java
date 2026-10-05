package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.*;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizPrescription;
import com.his.emr.vo.*;

import java.util.List;

/**
 * 医生工作站服务接口
 */
public interface EmrService extends IService<BizMedicalRecord> {

    /**
     * 查询病历列表（带统计信息）
     *
     * <p>患者身份的越权闸在这里：改个 patientId 就能读别人病历，所以收口在数据出口之前。
     */
    List<BizMedicalRecordVO> getByPatientId(MedicalRecordQueryDTO queryDTO);

    /**
     * 查询当前病历
     */
    BizMedicalRecordVO getByRegistId(Long registId);

    /**
     * 分页查询病历列表
     */
    PageResult<BizMedicalRecordVO> listPage(MedicalRecordQueryPageDTO queryDTO);

    /**
     * 获取病历详情（含处方、检查申请、检验申请）
     */
    EmrRecordDetailVO getRecordDetail(Long recordId);

    /**
     * 患者端·我的病历列表（只给已提交/已归档的，草稿是医生未完成的工作）
     */
    List<MyMedicalRecordVO> myRecords(Long patientId);

    /**
     * 患者端·病历详情。recordId 必须属于 patientId，否则按不存在处理 ——
     * 患者端只传 recordId 时不能让别人改个 ID 就读到别人的病历。
     */
    MyMedicalRecordVO myRecordDetail(Long patientId, Long recordId);

    /**
     * 审核病历
     */
    boolean reviewRecord(Long recordId, boolean approved, String remark, String reviewerName);

    /**
     * 新增处方
     */
    boolean addPrescription(BizPrescription prescription);

    /**
     * 获取处方详情（包含明细）
     */
    BizPrescription getPrescriptionDetail(Long prescriptionId);

    /**
     * 查询处方列表（不分页）
     */
    List<BizPrescription> selectPrescriptionList(Long patientId, Long doctorId, Integer prescriptionStatus);

    /**
     * 新增检查申请
     */
    boolean addInspectionApply(BizInspectionApply apply);

    /**
     * 查询检查申请列表（不分页）
     */
    List<BizInspectionApply> selectInspectionApplyList(Long patientId, Long doctorId);

    /**
     * 新增检验
     */
    boolean addLaboratoryApply(BizLaboratoryApply apply);

    /**
     * 查询检验申请列表（不分页）
     */
    List<BizLaboratoryApply> selectLaboratoryApplyList(Long patientId, Long doctorId);

    // 临时保存相关

    /**
     * 根据病历ID删除处方（用于临时保存时先删后增）
     */
    boolean deletePrescriptionsByRecordId(Long recordId);

    /**
     * 根据病历ID删除检查申请（用于临时保存时先删后增）
     */
    boolean deleteInspectionAppliesByRecordId(Long recordId);

    /**
     * 根据病历ID删除检验申请（用于临时保存时先删后增）
     */
    boolean deleteLaboratoryAppliesByRecordId(Long recordId);

    // 统一保存接口

    /**
     * 保存病历（临时保存/结诊共用）
     *
     * @param dto      病历保存DTO
     * @param isSubmit 是否提交（true=结诊，false=临时保存）
     * @return 保存后的病历ID
     */
    Long saveMedicalRecord(MedicalRecordSaveDTO dto, boolean isSubmit);

    // 检查/检验申请单（批次E：开单即落库 + 删除保护 + 结果回显）

    /**
     * 检查开单（**开单即落库**，不再等"保存病历"）。
     *
     * <p>{@code dto.id} 为空=新增，有值=修改（仅允许改「已提交未缴费」的单子）。
     * 患者信息、科室医生快照、项目编码名称与**价格**一律由后端按 {@code registId} + 字典补全 ——
     * 价格让前端传等于把定价权交给浏览器。
     *
     * @param dto 开单入参
     * @return 落库后的申请单（含执行进度字段）
     */
    BizInspectionApplyVO upsertInspectionApply(InspectionApplyUpsertDTO dto);

    /**
     * 检验开单（开单即落库），语义同 {@link #upsertInspectionApply(InspectionApplyUpsertDTO)}。
     *
     * @param dto 开单入参
     * @return 落库后的申请单（含执行进度字段）
     */
    BizLaboratoryApplyVO upsertLaboratoryApply(LaboratoryApplyUpsertDTO dto);

    /**
     * 删除检查申请单（带保护）。
     *
     * <p>只有「仍未缴费、未生成检查记录、未被收费单引用」的申请单允许删；
     * 其余一律拒绝并给出可读原因（例如「该申请单已缴费，请走退费流程」）。
     * 申请单是实现「已缴费」的事实依据，随便删掉会让收费单上的明细指不到来源。
     *
     * @param id 检查申请单 ID
     * @return true=删除成功
     */
    boolean deleteInspectionApply(Long id);

    /**
     * 删除检验申请单（带保护），语义同 {@link #deleteInspectionApply(Long)}。
     *
     * @param id 检验申请单 ID
     * @return true=删除成功
     */
    boolean deleteLaboratoryApply(Long id);

    /**
     * 按挂号查本次就诊的检查申请单（含执行进度、危急值、可删判定）。
     *
     * @param registId 挂号 ID
     * @return 申请单列表（按创建时间倒序）
     */
    List<BizInspectionApplyVO> listInspectionApplies(Long registId);

    /**
     * 按挂号查本次就诊的检验申请单（含执行进度、危急值、可删判定）。
     *
     * @param registId 挂号 ID
     * @return 申请单列表（按创建时间倒序）
     */
    List<BizLaboratoryApplyVO> listLaboratoryApplies(Long registId);
}
