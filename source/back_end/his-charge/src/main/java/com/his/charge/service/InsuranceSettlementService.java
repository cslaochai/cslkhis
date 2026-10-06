package com.his.charge.service;

import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.vo.BizInsuranceReportVO;
import com.his.charge.vo.BizInsuranceSettlementVO;
import com.his.charge.vo.InsuranceSettlementDetailVO;
import com.his.charge.vo.InsuranceStatsVO;
import com.his.charge.vo.PreSettlementVO;
import com.his.charge.vo.ReconcileResultVO;
import com.his.charge.vo.SettlementResultVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 医保结算服务接口
 */
public interface InsuranceSettlementService extends IService<BizInsuranceSettlement> {

    /**
     * 查询结算清单列表
     *
     * @param patientName 患者姓名，模糊匹配，可为空
     */
    PageResult<BizInsuranceSettlementVO> selectSettlementPage(Long patientId, String patientName,
                                                              Integer settlementStatus, int pageNum, int pageSize);

    /**
     * 获取结算清单详情
     */
    BizInsuranceSettlement getSettlementDetail(Long settlementId);

    /**
     * 单条清单出参：与列表同一套遮码口径，避免同一个人在列表与详情里证件号长短不一。
     */
    BizInsuranceSettlementVO getSettlementVO(Long settlementId);

    /**
     * 实体 → 列表 VO，顺手把证件号/医保卡号遮成 {@code xxxMasked}。
     *
     * <p>放在 service 而不是各 Controller 里抄：清单入口有两个（新 {@code /charge/settlement} 与
     * 旧 {@code /charge/insuranceListPage}），各写一份迟早遮成两种长度，看着像两个证号。
     */
    BizInsuranceSettlementVO toListVO(BizInsuranceSettlement entity);

    /**
     * 获取结算清单完整详情（关联患者、挂号、病历、结算账单及其账单行）
     */
    InsuranceSettlementDetailVO getSettlementDetailVO(Long settlementId);

    /**
     * 医保预结算：按账单与支付流水把「统筹 / 个账 / 自付」三个数算出来，不落库。
     */
    PreSettlementVO preSettlement(Long settlementId);

    /**
     * 正式结算：把 {@link #preSettlement} 算出的三个数写进清单，清单转 2-已结算。
     */
    SettlementResultVO settle(Long settlementId);

    /**
     * 上传结算清单（G7 报盘）：组装 2304 报文 → 落报文台账 → InsuranceChannelService 发送 →
     * 回执成功后清单状态 2→3。失败时台账保留失败记录，清单状态不变。
     */
    boolean uploadSettlement(Long settlementId);

    /**
     * 撤销已上传的结算清单（2305 报文）：回执成功后上传记录标「已被撤销」、清单回到 2-已结算
     */
    boolean cancelUpload(Long settlementId, String reason);

    /**
     * 报盘在医保侧还挂着账的清单（3-已上传 / 4-已审核）：这类账单要动钱，必须先用 2305 把报盘撤回来。
     *
     * <p>医保侧不存在「这张清单退了一半」的报文形态 —— 2305 是整单级冲正，
     * 硬发一次撤销等于把整单作废、把患者当年额度冲光，所以部分退只能拒绝。
     *
     * @return 没有已报盘的清单时返回 null（自费账单的正常路径）
     */
    BizInsuranceSettlement findReportedOfBill(Long billId);

    /**
     * 退费前的门禁：清单已报盘时只允许整单退。
     *
     * <p>判断放在动钱之前 —— 退到一半才发现报盘撤不掉，钱已经出去了，只能再手工补一张 2305。
     */
    void assertBillRefundable(Long billId, boolean wholeBill);

    /**
     * 账单作废 / 整单退清时的清单出口：已报盘的自动发 2305 撤回，然后一律置 5-已作废。
     *
     * <p>清单不跟着作废的话，它会永远停在「待结算」列表里，等着有人点一次结算、
     * 再往医保报一张账单已经作废的 2304。撤销是外发，失败即抛错让调用方整笔回滚：
     * 宁可作废不成功，也不能留下「账单没了、医保还记着这笔费用」的两张皮。
     *
     * <p>没有清单（自费账单）时直接返回。
     */
    void voidByBill(Long billId, String reason);

    /**
     * 账单发生<b>部分</b>退费后的出口：清单退回 1-待结算、个账与自付清零。
     *
     * <p>三个数是从「账单 + 当时的收款流水」现算的，钱一少它们就成了假数；
     * 让它停在 2-已结算等着被 2304 报上去，等于向医报一个患者没掏过的自付额。
     *
     * <p>已报盘（3/4）的清单不在本方法职责内 —— 那条路径由
     * {@link #assertBillRefundable} 提前拒绝，这里只静默跳过，不当第二个撤销入口。
     */
    void resetByBill(Long billId, String reason);

    /**
     * 报文台账分页（不含 payload/replyPayload 全文）
     */
    PageResult<BizInsuranceReportVO> selectReportPage(Long settlementId, Integer reportType, Integer status,
                                                      int pageNum, int pageSize);

    /**
     * 单条报文全文（前端「报盘原文」视图）
     */
    BizInsuranceReportVO getReportDetail(Long reportId);

    /**
     * 日对账：本地当日已报盘清单 vs 医保侧账单，输出汇总与差异
     */
    ReconcileResultVO reconcile(LocalDate billDate);

    /**
     * 审核结算清单：仅 3-已上传 → 4-已审核（驳回则留在已上传，等修正后重新报盘）
     */
    boolean auditSettlement(Long settlementId, boolean approved, String remark);

    /**
     * 出账时生成医保结算清单（L2 结算层的产物，sql/136）。
     *
     * <p>只对<b>产生了统筹记账</b>的账单出清单：全丙类、全自费的医保患者没有要报给医保局的钱，
     * 出一张统筹 0 元的清单等于让医保替你归档自费单据。
     *
     * <p>幂等：一张账单一张清单（唯一键 {@code uk_isb_bill}），重复调用只按最新账单金额刷新。
     *
     * @param coverageRatio 本次结算用的统筹报销比例（%），由 L2 的医保口径带下来
     */
    BizInsuranceSettlement generateFromBill(BizSettlementBill bill, List<BizSettlementBillItem> items,
                                            BigDecimal coverageRatio);

    /**
     * 医保工作台统计：今日结算金额/医保支付/单数 + 待结算与已结算数量。
     *
     * <p>放在服务而不是控制器：控制器不许直连 Mapper，且这组数是医保页首屏唯一数据源。
     */
    InsuranceStatsVO stats();
}
