package com.his.charge.service;

import com.his.charge.vo.InspectionRecordBrief;
import com.his.charge.vo.LabResultBrief;
import com.his.charge.vo.LaboratoryRecordBrief;
import java.time.LocalDate;
import java.util.List;

/**
 * 医技域对收费域提供的端口（依赖倒置）。
 *
 * <p>接口由消费方 his-charge 声明、his-medicaltech 实现，依赖方向固定为 medicaltech → charge。
 *
 * <p>分两类能力：
 * <ol>
 *   <li><b>读</b>：结算证据要检验/检查结论。查询口径（患者 + 就诊日，而不是创建时间）
 *       归医技域自己，charge 只声明"我要哪天的哪几条"。</li>
 *   <li><b>写</b>：缴费后由医技域按申请单幂等建执行记录、退费时撤销执行记录。
 *       收费域<b>不替医技决定能不能开工</b>，只发出动作，判定留在医技域。</li>
 * </ol>
 *
 * <p>写方法的实现方必须保证：执行记录<b>不存在则幂等创建</b>、<b>已开始的执行不可强撤</b>
 * （能不能撤是医技的判断，收费域不越权）。
 */
public interface MedicalTechGateway {

    /**
     * 某患者某就诊日的检验记录。
     *
     * @param patientId 患者ID（可空，返回空列表）
     * @param visitDate 就诊日期（可空 = 不限日期）
     */
    List<LaboratoryRecordBrief> listLaboratoryRecords(Long patientId, LocalDate visitDate);

    /**
     * 某患者某就诊日的检验结果项。
     *
     * <p>实现方内部按「记录 → 结果项」两段查：结果项挂在检验记录下，
     * 收费域不该知道这张父子关系，所以对外是一次平铺查询。</p>
     */
    List<LabResultBrief> listLabResults(Long patientId, LocalDate visitDate);

    /**
     * 某患者某就诊日的检查记录。
     */
    List<InspectionRecordBrief> listInspections(Long patientId, LocalDate visitDate);

    /**
     * 缴费成功后按检查申请单幂等创建执行记录（已存在则什么都不做）。
     *
     * @param applyId 检查申请单ID
     */
    void ensureInspectionRecordFromApply(Long applyId);

    /**
     * 缴费成功后按检验申请单幂等创建执行记录（已存在则什么都不做）。
     *
     * @param applyId 检验申请单ID
     */
    void ensureLaboratoryRecordFromApply(Long applyId);

    /**
     * 退费撤销：把该申请单下还没开始的检查执行记录置取消。
     *
     * <p>已经做过的检查不靠退费抹平 —— 能不能撤由医技域判断，
     * 撤不了就保持原样，收费域不重复这套判定。</p>
     *
     * @param applyId 检查申请单ID
     * @param reason  撤销原因（落审计）
     */
    void cancelInspectionByApplyId(Long applyId, String reason);

    /**
     * 退费撤销：把该申请单下还没开始的检验执行记录置取消。
     *
     * @param applyId 检验申请单ID
     * @param reason  撤销原因（落审计）
     */
    void cancelLaboratoryByApplyId(Long applyId, String reason);
}
