package com.his.charge.api;

import com.his.charge.service.FeeRecordService;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.AdmissionBriefVO;
import com.his.charge.vo.PatientBriefVO;

/**
 * 患者域对收费域提供的只读端口（依赖倒置）。
 */
public interface PatientGateway {

    /**
     * 按患者ID取患者摘要。
     *
     * @param patientId 患者ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    PatientBriefVO findPatient(Long patientId);

    /**
     * 按入院ID取入院摘要。
     *
     * @param admissionId 入院ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    AdmissionBriefVO findAdmission(Long admissionId);

    /**
     * 按住院医嘱单号反查开单科室（收费明细的科室归属反查入口）。
     *
     * <p>用<b>单号</b>而不是 ID：记账行的 {@code sourceId} 在不同来源语义不统一
     * （药品明细是处方明细 ID、住院医嘱是医嘱 ID），只有单号跨来源一致。
     *
     * @param orderNo 住院医嘱单号
     * @return 查不到返回 {@code null}（调用方保持科室为空，<b>不得兜底成默认科室</b>，
     * 兜底会让科室收入表凭空多出一块来路不明的钱）
     */
    ChargeDeptResolver.DeptRef findDeptByOrderNo(String orderNo);
}
