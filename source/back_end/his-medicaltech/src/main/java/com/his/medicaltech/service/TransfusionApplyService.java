package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.TransfusionApplyQueryPageDTO;
import com.his.medicaltech.dto.TransfusionApplyUpsertDTO;
import com.his.medicaltech.dto.TransfusionApproveDTO;
import com.his.medicaltech.dto.TransfusionCancelDTO;
import com.his.medicaltech.dto.TransfusionCrossmatchDTO;
import com.his.medicaltech.dto.TransfusionFinishDTO;
import com.his.medicaltech.dto.TransfusionIssueDTO;
import com.his.medicaltech.dto.TransfusionReactionDTO;
import com.his.medicaltech.dto.TransfusionStartDTO;
import com.his.patient.vo.CodeOptionVO;
import com.his.medicaltech.vo.TransfusionApplyVO;

import java.util.List;

/**
 * 住院输血闭环服务（P4.4）。
 *
 * <p>状态机（每一步都有明确的"谁能做、前提是什么"）：
 * <pre>
 *   0-待配血 ──配血（全部相合且配齐）──▶ 1-已配血 ──发血──▶ 2-已发血 ──双人核对开始输注──▶ 3-输注中 ──完成──▶ 4-已完成
 *       │                                    │
 *       └──────────────取消───────────────────┴──▶ 5-已取消
 * </pre>
 *
 * <p>三条不可破的前置条件：
 * <ul>
 *   <li><b>未配血不可发血</b>：相合性未知的血不能发给患者；</li>
 *   <li><b>未发血不可输注</b>：没有血袋号与发血记录，输进去的是什么无从追溯；</li>
 *   <li><b>输注中以后不可取消</b>：血已经进入患者体内，取消它是销毁证据。</li>
 * </ul>
 *
 * <p><b>配血阶段做两道硬拦</b>（这是整个闭环最要紧的校验）：ABO 相容性（红细胞与血浆方向相反）、
 * Rh 相容性（受血者 Rh 阴性时血袋必须 Rh 阴性）。ABO 不相容输注是致死性医疗差错，
 * 所以这两条一旦不满足<b>直接拒绝</b>，不是"提示"。
 *
 * <p><b>完成时回写</b>：住院病历（record_type=11 输血记录）+
 * 病案首页 {@code is_transfusion=1}，并把记录ID 回填到申请单。
 * 状态"已完成"而记录ID 为空 = 链断了，属于必须拦住的假数据。
 */
public interface TransfusionApplyService {

    /** 输血申请分页 */
    IPage<TransfusionApplyVO> listPage(TransfusionApplyQueryPageDTO query);

    /** 输血申请详情（含血袋明细） */
    TransfusionApplyVO getDetailById(Long applyId);

    /** 某次住院的全部输血申请（按申请时间升序 = 这条链的发生顺序） */
    List<TransfusionApplyVO> listByAdmission(Long admissionId);

    /** 发起 / 修改输血申请（返回输血申请单号；修改仅允许「待配血」） */
    String save(TransfusionApplyUpsertDTO dto);

    /** 用血分级审批（通过/驳回；驳回必填原因；急诊补审同样走这里） */
    String approve(TransfusionApproveDTO dto);

    /** 某单的审批流水（时间正序 = 逐级链的自然顺序） */
    List<TransfusionApplyVO.ApproveRecord> approveListByApply(Long applyId);

    /** 审批统计（按状态 + 按级别） */
    TransfusionApplyVO.ApproveStats approveStats();

    /**
     * 配血（逐袋录入血型鉴定与交叉配血结果）。
     *
     * <p><b>ABO / Rh 不相容直接拒绝整批</b>（操作错误，回滚不留痕）；
     * 但<b>交叉配血结论为「不合」是正常业务结果</b> —— 数据照常落库
     * （{@code crossmatch_status=3}，流程停在「待配血」），由返回值说明情况，不抛异常。
     * 抛异常会回滚掉刚录的血袋行，把"配了但不合"变成"看起来没配过"。
     *
     * @return 本次配血的结果说明（"全部相合，等待发血" / "N 袋不合，不能发血" / "已配 x/y 袋"）
     */
    String crossmatch(TransfusionCrossmatchDTO dto);

    /** 发血（已配血且全部相合 → 已发血） */
    void issue(TransfusionIssueDTO dto);

    /** 开始输注（含双人核对；已发血 → 输注中） */
    void startInfusion(TransfusionStartDTO dto);

    /** 完成（输注中 → 已完成；回写输血记录病历 + 首页 is_transfusion=1） */
    void finish(TransfusionFinishDTO dto);

    /** 输血反应上报（仅「已完成」且尚未上报；只置 has_reaction，不回改历史状态） */
    void reportReaction(TransfusionReactionDTO dto);

    /** 取消（仅待配血 / 已配血 / 已发血 → 已取消） */
    void cancel(TransfusionCancelDTO dto);

    /** 未完成输血数（工作台角标） */
    long countUnfinished(Long admissionId);

    /** 血液品种字典（前端渲染下拉） */
    List<CodeOptionVO> componentOptions();

    /** 输血前核对要点字典（前端渲染勾选框） */
    List<TransfusionApplyVO.CheckItem> checkItems();

    /** 输血反应类型字典（受控字典，前端只能选不能填） */
    List<String> reactionTypes();
}
