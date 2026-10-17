package com.his.api.service;

import com.his.api.vo.DeptRefVO;
import com.his.api.vo.MedicalRecordBriefVO;
import com.his.api.vo.PrescriptionBriefVO;
import com.his.api.vo.PrescriptionDetailBriefVO;
import com.his.api.vo.RecordBriefVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 病历域对外提供的端口（依赖倒置，实现落在 his-emr）：
 * 病历/处方查询、开单科室反查、缴费推进与退费撤销。
 */
public interface EmrProvider {

    // 病历查询

    /**
     * 按病历ID取简要信息（复诊关联「原病历」下拉用）。
     *
     * @param recordId 病历ID
     * @return 简要信息；不存在返回 null
     */
    RecordBriefVO getRecord(Long recordId);

    /**
     * 按患者取最近的就诊病历，供「原病历」下拉选择（窗口挂号/医生站/小程序共用）。
     *
     * <p>只要真正发生过的一次就诊（{@code registId} 非空）——草稿病历没有对应的号，
     * 选它当基准会让收费策略拿不到就诊日而判错。按就诊日倒序，同一天取最新一份。
     *
     * @param patientId 患者ID
     * @param limit     最多返回条数（&lt;=0 时由实现侧取默认值）
     * @return 候选列表；无病历返回空集合
     */
    List<RecordBriefVO> listRecentByPatient(Long patientId, int limit);

    /**
     * 按挂号ID取该次就诊最近一条病历。
     *
     * @return 查不到返回 {@code null}
     */
    MedicalRecordBriefVO findLatestMedicalRecordByRegist(Long registId);

    /**
     * 按患者ID取最近一条病历（挂号ID 查不到时的回退口径）。
     *
     * <p>回退会拿到<b>别的就诊</b>的病历，调用方必须据此提示人工复核，
     * 不能当成"本次就诊的病历"直接用。</p>
     *
     * @return 查不到返回 {@code null}
     */
    MedicalRecordBriefVO findLatestMedicalRecordByPatient(Long patientId);

    /**
     * 是否开过处方（入院适应证核查用）。
     *
     * <p>收费域只关心"有没有开过"，不读处方内容，所以这里返回布尔而不是列表 ——
     * 少往外传一份明细，就少一处将来会被误用的地方。</p>
     *
     * @param registId  挂号ID（优先口径，可空）
     * @param patientId 患者ID（registId 下查不到时的回退口径，可空）
     */
    boolean hasPrescriptions(Long registId, Long patientId);

    /**
     * 某次就诊的处方（按挂号ID）。
     */
    List<PrescriptionBriefVO> listPrescriptionsByRegist(Long registId);

    /**
     * 某患者的处方（挂号ID 下查不到时的回退口径）。
     */
    List<PrescriptionBriefVO> listPrescriptionsByPatient(Long patientId);

    /**
     * 处方明细（诊断依据关键词匹配只要药名）。
     *
     * @param prescriptionIds 处方ID清单（可空，返回空列表）
     */
    List<PrescriptionDetailBriefVO> listPrescriptionDetails(List<Long> prescriptionIds);

    /**
     * 按处方号反查开单科室。
     *
     * @return 查不到返回 {@code null}，<b>不得兜底成默认科室</b>
     */
    DeptRefVO findDeptByPrescriptionNo(String prescriptionNo);

    /**
     * 按检查申请单号反查开单科室。
     *
     * @return 查不到返回 {@code null}，<b>不得兜底成默认科室</b>
     */
    DeptRefVO findDeptByInspectionApplyNo(String applyNo);

    /**
     * 按检验申请单号反查开单科室。
     *
     * @return 查不到返回 {@code null}，<b>不得兜底成默认科室</b>
     */
    DeptRefVO findDeptByLaboratoryApplyNo(String applyNo);

    // 缴费推进 / 退费撤销：动作发出去，能不能做由病历域判断

    /**
     * 处方明细已缴费，推进到已缴费状态。
     *
     * @param prescriptionDetailId 处方明细ID
     * @param paidAmount           本次支付金额
     * @param payMethod            支付渠道
     */
    void advancePrescriptionDetail(Long prescriptionDetailId, BigDecimal paidAmount, Integer payMethod);

    /**
     * 处方明细被红冲，退回未缴费。
     *
     * @param prescriptionDetailId 处方明细ID
     * @param reason               撤销原因（落审计）
     */
    void revertPrescriptionDetail(Long prescriptionDetailId, String reason);

    /**
     * 检验申请单已缴费
     */
    void advanceLaboratoryApply(Long applyId);

    /**
     * 检验申请单被撤销，退回未缴费。
     */
    void revertLaboratoryApply(Long applyId);

    /**
     * 检查申请单已缴费
     */
    void advanceInspectionApply(Long applyId);

    /**
     * 检查申请单被撤销，退回未缴费。
     */
    void revertInspectionApply(Long applyId);

    /**
     * 退费前置闸门：这些处方明细里有药还没退就不许退费。
     *
     * @param prescriptionDetailIds 处方明细ID清单
     * @param scene                 场景（异常文案用）
     * @throws com.his.common.exception.BusinessException 存在未退药时抛出
     */
    void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene);
}
