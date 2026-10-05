package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.*;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientOperation;
import com.his.patient.vo.*;

import java.util.List;

/**
 * 住院管理（第 1 期：入出院闭环 + 病案首页）
 */
public interface InpatientService {

    /**
     * 住院列表分页（在院 / 已出院）
     */
    IPage<InpatientVO> listPage(InpatientQueryPageDTO query);

    /**
     * 当前登录员工所在科室的住院列表分页（医生站/护士站左栏工作列表）。
     * <p>deptId 一律服务端取 {@code UserUtils.getCurrentUser()}，**不接收前端传的科室**：
     * 医生站的数据边界是本科室（跨科患者走会诊邀请），让前端传 deptId 等于全院数据对任何登录者敞开。
     */
    IPage<InpatientVO> listMyDeptPage(InpatientQueryPageDTO query);

    /**
     * 住院详情（入院信息 + 病案首页 + 诊断明细 + 手术明细）
     */
    InpatientDetailVO detail(Long admissionId);

    /**
     * 入院登记（分床 + 占床 + 生成住院号 + 初始化首页草稿），返回入院ID
     */
    Long admit(InpatientAdmitDTO dto);

    /**
     * 换床（限同一科室内部）
     */
    void transfer(InpatientTransferDTO dto);

    /**
     * 出院办理（写出院记录 + 释放床位 + 回写病案首页）
     */
    void discharge(InpatientDischargeDTO dto);

    /**
     * 保存病案首页（含诊断 / 手术明细，整表替换）
     */
    InpatientDetailVO saveSummary(InpatientSummaryUpsertDTO dto);

    /**
     * 住院统计卡片
     */
    InpatientStatsVO stats();

    /**
     * 病区列表（床位数实时取自床位）
     */
    List<WardVO> listWards();

    /**
     * 床位列表（可选床位图 / 选床）
     */
    List<BedVO> listBeds(Long wardId, Long deptId, Integer bedStatus);

    /**
     * 病区床位图（一床一卡，含空床；科室按登录态强制收口）。
     * <p>与 {@link #listBeds} 的区别：那个服务入院分床/换床，只要床位本身；
     * 这个要把在院患者、护理级别、主管医生、术后天数一次带齐，且必须收口到当前岗位科室，
     * 两者查询形状不同，合一个接口只会互相拖慢。
     */
    BedMapVO bedMap(BedMapQueryDTO query);

    /**
     * 急诊留观占床：只占床位（不生成入院记录），床位非空闲直接拒绝。
     * <p>留观不占床位的话，「这张床有没有人躺着的」事实就只存在于急诊记录一处，
     * 住院分床会把同一张床再发出去——占床动作必须归床位的所有者（本服务）做。
     */
    void occupyBedForObservation(Long bedId, Long patientId);

    /**
     * 急诊留观释放床位；床位已挂在别人名下（如已转住院）时静默不动，绝不释放别人的床
     */
    void releaseBedForObservation(Long bedId, Long patientId);

    /**
     * 入院单快照（住院状态与就诊科室/病区/床位归属）；不存在返回 {@code null}
     */
    BizAdmission getAdmissionById(Long admissionId);

    /**
     * 病区（含所属科室快照）；不存在返回 {@code null}
     */
    WardVO getWardById(Long wardId);

    /**
     * 床位号；床位不存在返回 {@code null}
     */
    String getBedNoById(Long bedId);

    /**
     * 病案首页是否已归档。
     * <p>「归档后不可改」是首页这一事实的写入闸门，由首页方判定，别让每个回写方各抄一份。
     */
    boolean isSummaryArchived(Long admissionId);

    /**
     * 手术闭环回写首页手术明细：拦「首页已有主要手术」与「首页手工录过同名手术」，
     * 落库后统一重排手术序号，并把首页标成手术病例。
     *
     * @return 落库后的明细行（含ID，供手术单回指）
     */
    BizInpatientOperation appendSurgeryOperation(BizInpatientOperation operation);

    /**
     * 把首页标记为「输血病例」。首页是"这台住院发生过什么"的汇总事实，标记只由首页方写。
     * <p>首页可能要等出院结算后才建，输血发生时常常还不存在 —— 此时静默跳过，不阻断输血完成。
     */
    void markSummaryTransfused(Long admissionId);
}
