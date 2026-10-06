package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.charge.mapper.BizDaySettlementMapper;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 院级日结单（G8）。一天一张，{@code settleDate} 唯一。
 *
 * <p><b>本单的金额来自三条互相独立的链，三级对账才有判别力</b>（详见
 * {@link BizDaySettlementMapper}）：
 * <ol>
 *   <li><b>资金链（L3 支付流水）</b>—— {@code chargeAmount}/{@code refundAmount}/各渠道分桶/
 *       {@code billCount} 全部现算自支付资金流水。这是"今天到底进了多少钱"的唯一事实。</li>
 *   <li><b>凭证链（L4 班结单）</b>—— {@code detailAmount} = 当日各交班单定格金额之和。
 *       它落后于资金链就说明<b>有人没收了钱不交班</b>。</li>
 *   <li><b>账单链（L2 账单 + 摊行）</b>—— {@code deptAmount}/{@code unattributedAmount} 按科室归集
 *       当日收讫账单的明细摊行，与单头应收合计互校。</li>
 * </ol>
 * 三级对账因此是：一级逐张班结 ↔ 该时段流水复算；二级 Σ班结 ↔ 全院流水实收；
 * 三级 Σ摊行（含无归属）↔ Σ账单单头。任何一级自己等于自己都毫无意义，所以左右必须来自不同的表或不同的取数路径。
 *
 * <p><b>统筹（{@link #poolAmount}）只在本院出现</b>：它是医保局后付给医院的钱，
 * 收银员不经手、不进现金清点、也不参与 {@link #netAmount}，单列出来与医保报盘台账核对。
 *
 * <p>二级和三级必须同时成立才算"平"。只对一级的话，"某个收费员忘了交班"永远查不出来
 * —— 所以另有 {@link #unassignedCount}/{@link #unassignedAmount} 专门装这类流水。
 *
 * <p><b>科室维度</b>：{@link #deptAmount} 是已归到科室的摊行金额，{@link #unattributedAmount}
 * 是**科室锚点缺失**的摊行金额。两者之和才等于摊行总额 —— 无归属的绝不并进科室统计，
 * 否则科室收入表会凭空多出一块来路不明的钱。新数据（记账时就带科室）应当 100% 有归属，
 * 这个字段是给缺陷留的观察窗。
 *
 * <p><b>可重算但不可改</b>：{@code settleStatus=1}（待审核）时允许重新汇总覆盖（当天数据还在变），
 * 一经审核（2）即锁定 —— 防止"白天对完账、晚上偷偷改数"。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_day_settlement")
public class BizDaySettlement extends BaseEntity {

    /**
     * 日结单号（唯一，格式 RJ + yyyyMMdd）
     */
    private String settlementNo;

    /**
     * 日结日期（按班结单 period_end / 收费时间所在自然日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settleDate;

    /**
     * 纳入的班结单数
     */
    private Integer shiftCount;

    /**
     * 收费笔数（当日全院收款流水笔数）
     */
    private Integer chargeCount;

    /**
     * 收费金额（当日全院实收合计，资金链现算）
     */
    private BigDecimal chargeAmount;

    /**
     * 当日发生过收款的账单数（去重 bill_id，一层时代没有这个口径）
     */
    private Integer billCount;

    /**
     * 退费笔数
     */
    private Integer refundCount;

    /**
     * 退费金额（正数）
     */
    private BigDecimal refundAmount;

    /**
     * 净额 = 收费 - 退费
     */
    private BigDecimal netAmount;

    /**
     * 现金
     */
    private BigDecimal cashAmount;

    /**
     * 微信
     */
    private BigDecimal wechatAmount;

    /**
     * 支付宝
     */
    private BigDecimal alipayAmount;

    /**
     * 医保个账（刷参保人卡扣的额度，是一笔真实收款）
     */
    private BigDecimal insuranceAmount;

    /**
     * 院内余额
     */
    private BigDecimal balanceAmount;

    /**
     * 没有对应渠道列的金额（支付方式缺失 + 银行卡/转账）
     */
    private BigDecimal unknownPayAmount;

    /**
     * 医保统筹记账额（当日收讫账单合计；不进 {@link #netAmount}，与报盘台账核对）
     */
    private BigDecimal poolAmount;

    /**
     * 开票张数（票跟着账单走：当日收讫账单所开的票）
     */
    private Integer invoiceCount;

    /**
     * 作废张数（含红冲换开的原票）
     */
    private Integer invoiceVoidCount;

    /**
     * Σ交班单定格金额（凭证链合计，二级对账左值；与 {@link #chargeAmount} 这条资金链对照）
     */
    private BigDecimal detailAmount;

    /**
     * 有科室归属的科室数
     */
    private Integer deptCount;

    /**
     * 已归属科室的摊行金额合计（毛收入，未扣优惠/统筹）
     */
    private BigDecimal deptAmount;

    /**
     * 无科室归属的摊行金额合计（单列差异项，不并入科室统计）
     */
    private BigDecimal unattributedAmount;

    /**
     * 未纳入任何班结单的收款流水笔数（忘交班 / 交完班又收钱 / 患者端自助缴费）
     */
    private Integer unassignedCount;

    /**
     * 未纳入班结的金额
     */
    private BigDecimal unassignedAmount;

    /**
     * 对账结论（1-已平 2-有差异）
     */
    private Integer reconcileStatus;

    /**
     * 最大差异金额（各级对不上的绝对值最大者）
     */
    private BigDecimal diffAmount;

    /**
     * 差异明细（逐条说明哪一级差了哪几笔，人可读文本）
     */
    private String diffDetail;

    /**
     * 状态（1-待审核 2-已审核；已审核后不可重算）
     */
    private Integer settleStatus;

    /**
     * 日结人
     */
    private String settleBy;

    /**
     * 日结时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settleTime;

    /**
     * 审核人
     */
    private String auditBy;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    private String auditRemark;
}
