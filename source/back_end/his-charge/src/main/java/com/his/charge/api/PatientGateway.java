package com.his.charge.api;

import com.his.charge.service.FeeRecordService;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.AdmissionBriefVO;
import com.his.charge.vo.PatientBriefVO;

/**
 * 患者域对收费域提供的只读端口（依赖倒置）。
 *
 * <p><b>接口为什么定义在 charge 而不是 patient：</b>记账能力（六家都要调
 * {@link FeeRecordService}）最终要落在 his-charge，于是 his-charge 必须是最底层模块 ——
 * 它不能反过来依赖 his-patient，否则 Maven reactor 出现 patient ↔ charge 环，直接拒绝构建。
 * 所以 charge 声明自己需要什么、由 patient 来实现。
 *
 * <p>这也正是「A 依赖 B 只能依赖 B 的 service，不能碰 B 的 Mapper 和实体」这条规矩的落法：
 * charge 拿到的是 service 接口 + 自己声明的 DTO，不是 {@code BizPatientMapper} 和
 * {@code BizPatient}。实现方 patient 内部怎么查（自己的实体、自己的 Mapper）charge 不关心。
 *
 * <p>实现约定：
 * <ul>
 *   <li>用 Spring 注入，<b>不要</b>用 {@code ObjectProvider} 惰性获取做降级 ——
 *       这是必需能力，缺实现属于部署事故，不属于可降级路径；</li>
 *   <li>只读方法，必须尊重 {@code del_flag}；</li>
 *   <li>查不到返回 {@code null}，由调用方决定是抛错还是留痕，port 不替调用方做业务判断。</li>
 * </ul>
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
     *         兜底会让科室收入表凭空多出一块来路不明的钱）
     */
    ChargeDeptResolver.DeptRef findDeptByOrderNo(String orderNo);
}
