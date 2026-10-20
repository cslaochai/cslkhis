package com.his.spi.service;

import com.his.spi.vo.ExecStatusVO;
import com.his.spi.vo.InspectionRecordBriefVO;
import com.his.spi.vo.LabItemVO;
import com.his.spi.vo.LabResultBriefVO;
import com.his.spi.vo.LaboratoryRecordBriefVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 医技域对外提供的端口（依赖倒置，实现落在 his-medicaltech）：
 * 检查/检验记录与结果查询、缴费推进、执行进度查询。
 */
public interface MedicalTechFacadeService {

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

    // 申请单执行进度（结果回显到医生站）

    /**
     * 批量查询检查申请单的执行进度。
     *
     * @param applyIds 检查申请单 ID 集合（可为空）
     * @return 与入参对应的执行进度；没有执行记录的申请单不会有返回项
     */
    List<ExecStatusVO> listInspectionExecStatus(List<Long> applyIds);

    /**
     * 批量查询检验申请单的执行进度，语义同 {@link #listInspectionExecStatus(List)}。
     *
     * @param applyIds 检验申请单 ID 集合（可为空）
     * @return 执行进度列表
     */
    List<ExecStatusVO> listLaboratoryExecStatus(List<Long> applyIds);

    /**
     * 按检验项目 ID 取开单用的项目快照（编码/名称/科室/价格/标本类型）。
     *
     * @param itemId 检验项目 ID（可空）
     * @return 项目不存在时返回 {@code null}，报错文案由开单方自己决定
     */
    LabItemVO findLabItem(Long itemId);

    /**
     * 按检查项目 ID 取开单用的项目快照（编码/名称/归属科室/价格），语义同 {@link #findLabItem(Long)}。
     *
     * <p>检查项目与检验项目是两张字典表，但开单方要抄的快照列一致，所以共用同一个出参类型。</p>
     *
     * @param itemId 检查项目 ID（可空）
     * @return 项目不存在时返回 {@code null}，报错文案由开单方自己决定
     */
    LabItemVO findInspectionItem(Long itemId);
}
