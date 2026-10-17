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
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 院级日结单（G8）。一天一张，settleDate 唯一。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_day_settlement")
public class BizDaySettlement extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
