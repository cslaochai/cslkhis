package com.his.medicaltech.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.medicaltech.dto.LabResultSaveDTO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.vo.*;

import java.util.List;

/**
 * 医技服务接口
 */
public interface MedicalTechService extends IService<BizInspectionRecord> {

    /**
     * 查询检查记录列表
     */
    PageResult<BizInspectionRecord> selectInspectionRecordPage(Long patientId, Long inspectionDeptId, int pageNum, int pageSize);

    PageResult<BizInspectionRecordVO> selectInspectionRecordPageVO(Long patientId, Long inspectionDeptId, int pageNum, int pageSize);

    /**
     * 查询检查记录列表（不分页）
     */
    List<BizInspectionRecord> selectInspectionRecordList(Long patientId, Long inspectionDeptId);

    List<BizInspectionRecordVO> selectInspectionRecordListVO(Long patientId, Long inspectionDeptId);

    /**
     * 获取检查记录详情
     */
    InspectionDetailVO getInspectionDetail(Long recordId);

    /**
     * 检查签到
     */
    boolean checkIn(Long recordId);

    /**
     * 开始检查
     */
    boolean startInspection(Long recordId);

    /**
     * 执行检查（**非放射**项目：超声 / 心电 / 内镜 / 其他）。
     *
     * <p>放射类（检查项目字典的项目类型 = 1）从这里被**硬拒绝** ——
     * 它们的报告只能由诊断医师在放射诊断工作站（sql/138）书写，
     * 技师的动作到 {@link #finishShoot(Long)} 为止。
     */
    boolean executeInspection(Long recordId, String executeBy, String resultDescription,
                              String resultConclusion, String suggestions);

    /**
     * 拍片完成（**放射**项目专用，技师岗的终点，sql/138）。
     *
     * <p>只做三件事：记执行人与执行时间、把记录推进到「已出结果」。
     * **不建报告、不签名** —— 这两件事现在是放射诊断医师的活
     * （{@code RadiologyReportService.submit / audit}）。
     *
     * <p>为什么非要单独一个接口而不是复用 executeInspection 传空内容：
     * 复用就意味着「写结果」这个方法允许传空，那么技师随手点一下也会建出一份空报告，
     * 分岗等于没分。两个动作必须是两个入口，服务端才拦得住。
     *
     * @param recordId 检查记录ID
     * @return true
     * @throws com.his.common.exception.BusinessException 不是放射项目时拒绝（非放射走 executeInspection）
     */
    boolean finishShoot(Long recordId);

    /**
     * 审核检查报告
     */
    boolean auditInspection(Long recordId, String auditBy);

    /**
     * 查询检验记录列表
     */
    PageResult<BizLaboratoryRecord> selectLaboratoryRecordPage(Long patientId, Long laboratoryDeptId, int pageNum, int pageSize);

    PageResult<BizLaboratoryRecordVO> selectLaboratoryRecordPageVO(Long patientId, Long laboratoryDeptId, int pageNum, int pageSize);

    /**
     * 查询检验记录列表（不分页）
     */
    List<BizLaboratoryRecord> selectLaboratoryRecordList(Long patientId, Long laboratoryDeptId);

    List<BizLaboratoryRecordVO> selectLaboratoryRecordListVO(Long patientId, Long laboratoryDeptId);

    /**
     * 获取检验记录详情
     */
    LaboratoryDetailVO getLaboratoryDetail(Long recordId);

    boolean saveResult(LabResultSaveDTO labResultSaveDTO);

    /**
     * 接收标本
     */
    boolean receiveSpecimen(Long recordId, String receiveBy);

    /**
     * 录入检验结果
     */
    boolean inputLabResult(Long recordId, String executeBy, List<BizLabResult> results,
                           String diagnosis, String suggestions);

    /**
     * 审核检验报告
     */
    boolean auditLaboratory(Long recordId, String auditBy);

    /**
     * 查询报告列表
     */
    PageResult<BizReport> selectReportPage(Long patientId, Integer reportType, Integer reportStatus,
                                           int pageNum, int pageSize);

    PageResult<BizReportVO> selectReportPageVO(Long patientId, Integer reportType, Integer reportStatus,
                                               int pageNum, int pageSize);

    /**
     * 获取报告详情
     */
    BizReport getReportDetail(Long reportId);

    /**
     * 获取报告详情（含影像帧），报告不存在时返回 null
     */
    BizReportVO getReportDetailVO(Long reportId);

    /**
     * 发布报告
     */
    boolean publishReport(Long reportId, String publishBy);

    // 标本管理

    /**
     * 分页查询标本列表
     */
    PageResult<BizLaboratoryRecord> selectSpecimenPage(Long patientId, Integer recordStatus,
                                                       String keyword, int pageNum, int pageSize);

    PageResult<BizLaboratoryRecordVO> selectSpecimenPageVO(Long patientId, Integer recordStatus,
                                                           String keyword, int pageNum, int pageSize);

    /**
     * 分配标本条码
     */
    boolean assignBarcode(Long recordId, String specimenNo);

    /**
     * 标本采集确认
     */
    boolean sampleSpecimen(Long recordId, String sampleBy);

    /**
     * 标本退回
     */
    boolean rejectSpecimen(Long recordId, String reason);

    /**
     * 标本统计（各状态数量）
     */
    SpecimenStatsVO getSpecimenStats();

    // 缴费驱动建执行记录（批次E：从 his-charge 迁入）

    /**
     * 按检查申请单**幂等**生成检查记录（患者缴费时调用）。
     *
     * <p>为什么必须幂等：这个动作原先写在 {@code ChargeController.processPayment} 里，
     * 直接 {@code inspectionRecordMapper.insert(record)}，没有任何存在性判断。
     * 实测库中已出现同一个申请ID 挂着 5 条检查记录 ——
     * 收费单被重复结算（退费重缴、重复提交、并发点击）就会多出几条「已登记」的检查记录，
     * 患者去检查科会被叫号两次、结果回填不知道该写哪一条。
     *
     * <p>记录由「已缴费」驱动创建，初始状态固定为 1 已登记。
     *
     * @param applyId 检查申请单 ID
     * @return 检查记录 ID（已存在则返回既有那条的 ID）
     */
    Long ensureInspectionRecordFromApply(Long applyId);

    /**
     * 按检验申请单**幂等**生成检验记录（患者缴费时调用），语义同
     * {@link #ensureInspectionRecordFromApply(Long)}。
     *
     * @param applyId 检验申请单 ID
     * @return 检验记录 ID（已存在则返回既有那条的 ID）
     */
    Long ensureLaboratoryRecordFromApply(Long applyId);

    /**
     * 退费时取消**尚未开始**的检查执行记录（批次E：退费闭环）。
     *
     * <p>原实现退费只改收费单状态，申请单仍停在「已缴费」、执行记录仍是「已登记」——
     * 患者钱退了，检查科的工作台上还挂着他的待检项。
     *
     * @param applyId 检查申请单 ID
     * @param reason  取消原因，写入 {@code cancel_reason}
     * @return true=确实取消了一条记录；false=本来就没有记录（幂等，不报错）
     * @throws com.his.common.exception.BusinessException 记录已开始执行（状态 &ge; 3 检查中）时拒绝退费
     */
    boolean cancelInspectionByApplyId(Long applyId, String reason);

    /**
     * 退费时取消**尚未开始**的检验执行记录，语义同
     * {@link #cancelInspectionByApplyId(Long, String)}。
     *
     * @param applyId 检验申请单 ID
     * @param reason  取消原因
     * @return true=确实取消了一条记录；false=本来就没有记录
     */
    boolean cancelLaboratoryByApplyId(Long applyId, String reason);
}
