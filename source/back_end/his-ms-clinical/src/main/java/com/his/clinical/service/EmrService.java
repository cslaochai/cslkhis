package com.his.clinical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.clinical.dto.*;
import com.his.clinical.entity.BizInspectionApply;
import com.his.clinical.entity.BizLaboratoryApply;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizPrescription;
import com.his.clinical.support.QcOperator;
import com.his.clinical.vo.*;
import com.his.common.base.PageResult;
import com.his.spi.vo.MyMedicalRecordVO;

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
    PageResult<BizMedicalRecordVO> listPage(MedicalRecordPageQueryDTO queryDTO);

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

    /**
     * 结诊事务提交后的异步收尾：只跑规则质控。
     *
     * <p>PDF（引导单/处方签）不在结诊时生成，改为医生点「打印就诊文书」按钮时按需现场画
     * （见 {@link #printVisitDocs(Long)}）。由 AFTER_COMMIT + @Async 触发。
     *
     * @param operator 结诊医生身份快照，取自发布事件时的登录态 —— 收尾线程里没有登录态，只能由调用方带进来
     */
    void asyncAfterSubmit(Long recordId, QcOperator operator);

    /**
     * 医生点「打印就诊文书」时现场生成三份 PDF：引导单 + 普通处方签（西药+中成药）+ 饮片处方签。
     *
     * <p>每次都重画（患者可能中途加了检查/换了药），没有对应内容就对应字段为空。
     */
    VisitDocsPrintVO printVisitDocs(Long recordId);

    /**
     * 医生撤回结诊：已提交(2) → 草稿(1)，把病历重新开放编辑。
     *
     * <p>闸门（真实 HIS 口径，任一不满足即拒绝并给出可读原因）：
     * <ul>
     *   <li>病历状态必须是已提交(2)；已归档(3)/已作废(4) 不允许医生撤回（走病案修改申请）；</li>
     *   <li>必须是本人开的病历（doctorId = 当前登录医生）；</li>
     *   <li>当日就诊（visitDate = 今天），隔日不允许医生自助撤回；</li>
     *   <li>未缴费：本次就诊没有已支付的账单（已支付走收费台退费，不在这里撤回）；</li>
     *   <li>未执行：检查/检验未出报告、处方未发药。</li>
     * </ul>
     * 反向动作同事务：病历状态回退 + 签名作废 + L1 记账行红冲 + 队列/挂号回退就诊中 + 归档记录作废。
     *
     * @param recordId 病历ID
     * @param queueId  患者队列行ID（前端当前患者带的 queueId；结诊回退队列用）
     * @param reason   撤回原因（留痕）
     */
    void revokeRecord(Long recordId, Long queueId, String reason);
}
