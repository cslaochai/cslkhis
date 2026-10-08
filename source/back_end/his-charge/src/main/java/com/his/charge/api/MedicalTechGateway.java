package com.his.charge.api;

import com.his.charge.vo.InspectionRecordBriefVO;
import com.his.charge.vo.LabResultBriefVO;
import com.his.charge.vo.LaboratoryRecordBriefVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 医技域对收费域提供的端口（依赖倒置）。
 */
public interface MedicalTechGateway {

    /**
     * 某患者某就诊日的检验记录。
     *
     * @param patientId 患者ID（可空，返回空列表）
     * @param visitDate 就诊日期（可空 = 不限日期）
     */
    List<LaboratoryRecordBriefVO> listLaboratoryRecords(Long patientId, LocalDate visitDate);

    /**
     * 某患者某就诊日的检验结果项。
     *
     * <p>实现方内部按「记录 → 结果项」两段查：结果项挂在检验记录下，
     * 收费域不该知道这张父子关系，所以对外是一次平铺查询。</p>
     */
    List<LabResultBriefVO> listLabResults(Long patientId, LocalDate visitDate);

    /**
     * 某患者某就诊日的检查记录。
     */
    List<InspectionRecordBriefVO> listInspections(Long patientId, LocalDate visitDate);

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
