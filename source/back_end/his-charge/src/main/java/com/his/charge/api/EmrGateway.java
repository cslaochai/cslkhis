package com.his.charge.api;

import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.MedicalRecordBriefVO;
import com.his.charge.vo.PrescriptionBriefVO;
import com.his.charge.vo.PrescriptionDetailBriefVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 病历域对收费域提供的端口（依赖倒置）。
 */
public interface EmrGateway {

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
    ChargeDeptResolver.DeptRef findDeptByPrescriptionNo(String prescriptionNo);

    /**
     * 按检查申请单号反查开单科室。
     *
     * @return 查不到返回 {@code null}，<b>不得兜底成默认科室</b>
     */
    ChargeDeptResolver.DeptRef findDeptByInspectionApplyNo(String applyNo);

    /**
     * 按检验申请单号反查开单科室。
     *
     * @return 查不到返回 {@code null}，<b>不得兜底成默认科室</b>
     */
    ChargeDeptResolver.DeptRef findDeptByLaboratoryApplyNo(String applyNo);

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
