package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收费员交班单（班结，G8）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cashier_settlement")
public class BizCashierSettlement extends BaseEntity {

    /**
     * 交班单号（唯一，格式 JS + yyyyMMdd + 4 位序号）
     */
    private String settlementNo;

    /**
     * 收费员工号（服务端取登录态写入，不信前端）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cashierId;

    /**
     * 收费员姓名（交班时快照）
     */
    private String cashierName;

    /**
     * 班次（1-白班 2-夜班 3-其他）。仅作展示标签，不影响统计区间。
     */
    private Integer shiftType;

    /**
     * 统计区间起（不含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 统计区间止（含，= 本次交班时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 所属院级日结单ID（null = 尚未日结）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long daySettlementId;

    /**
     * 收费笔数（本时段内经手的<b>收款流水</b>笔数，= 支付资金流水方向=收、状态=成功）
     */
    private Integer chargeCount;

    /**
     * 收费金额（本时段收款流水实收合计）
     */
    private BigDecimal chargeAmount;

    /**
     * 退费笔数（本时段退款流水笔数，按退款<b>发生时刻</b>归班，不按原收费日）
     */
    private Integer refundCount;

    /**
     * 退费金额（正数；符号只在 {@link #netAmount} 那一步参与）
     */
    private BigDecimal refundAmount;

    /**
     * 净额 = 收费金额 - 退费金额
     */
    private BigDecimal netAmount;

    /**
     * 现金（支付方式 1）
     */
    private BigDecimal cashAmount;

    /**
     * 微信（2）
     */
    private BigDecimal wechatAmount;

    /**
     * 支付宝（3）
     */
    private BigDecimal alipayAmount;

    /**
     * 医保个账（4）。
     *
     * <p>只有个账在这里 —— 刷的是参保人卡里的额度，是一笔真实收款，收银员要为它交班。
     * <b>统筹不在本表</b>：统筹是医保局后付给医院的钱，收银员不经手，硬塞进来会让现金清点
     * 凭空多出一块说不来的差额；它只在院级日结单出现。
     */
    private BigDecimal insuranceAmount;

    /**
     * 院内余额（5）
     */
    private BigDecimal balanceAmount;

    /**
     * 没有对应渠道列的金额：支付方式缺失（null）+ 6-银行卡、7-转账。
     *
     * <p>单列而不是并进任一渠道：并进现金会让收费员"点钞对不上"，并进微信会让渠道对账多出假数。
     * 正确做法是让它显式可见，逼出"为什么这笔钱没落在已知渠道上"。
     * 它是现金+微信+支付宝+个账+余额+本列 = 收费金额这个恒等式的补数，不是垃圾桶。
     */
    private BigDecimal unknownPayAmount;

    /**
     * 本时段开票张数（票跟着账单走：本时段收款流水涉及的账单所开的票）
     */
    private Integer invoiceCount;

    /**
     * 本时段作废张数（3-已作废 + 4-已红冲换开的原票，都是交不出去的废票根）
     */
    private Integer invoiceVoidCount;

    /**
     * 实交现金（收费员清点后录入）。系统现金有争议时，以"人点出来的"为准去反查差异。
     */
    private BigDecimal handinCash;

    /**
     * 现金差异 = 实交现金 - 系统现金；0 为平
     */
    private BigDecimal cashDiff;

    /**
     * 差异说明（差异非 0 时必填）
     */
    private String diffReason;

    /**
     * 状态（1-已交班待日结 2-已日结 3-已审核）
     */
    private Integer settleStatus;

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
