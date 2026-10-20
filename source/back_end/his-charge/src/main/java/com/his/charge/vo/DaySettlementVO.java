package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 院级日结单出参。
 */
@Data
public class DaySettlementVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 日结单号
     */
    private String settlementNo;

    /**
     * 日结日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settleDate;

    /**
     * 纳入的班结单数
     */
    private Integer shiftCount;

    /**
     * 收费笔数
     */
    private Integer chargeCount;

    /**
     * 收费金额
     */
    private BigDecimal chargeAmount;

    /**
     * 当日发生过收款的账单数（四层后才有：一笔业务可能多张账单、一张账单多笔流水）
     */
    private Integer billCount;

    /**
     * 退费笔数
     */
    private Integer refundCount;

    /**
     * 退费金额
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
     * 医保
     */
    private BigDecimal insuranceAmount;

    /**
     * 余额
     */
    private BigDecimal balanceAmount;

    /**
     * 支付方式未知金额
     */
    private BigDecimal unknownPayAmount;

    /**
     * 医保统筹记账额（不进现金清点、不参与净额，与报盘台账核对）
     */
    private BigDecimal poolAmount;

    /**
     * 开票张数
     */
    private Integer invoiceCount;

    /**
     * 作废张数
     */
    private Integer invoiceVoidCount;

    /**
     * 三级对账：Σ交班单定格金额（凭证链，与上面 chargeAmount 这条资金链对照）
     */
    private BigDecimal detailAmount;

    /**
     * 有科室归属的科室数
     */
    private Integer deptCount;

    /**
     * 已归属科室的明细金额合计
     */
    private BigDecimal deptAmount;

    /**
     * 无科室归属的明细金额合计
     */
    private BigDecimal unattributedAmount;

    /**
     * 未纳入任何班结单的已收费笔数
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
     * 最大差异金额
     */
    private BigDecimal diffAmount;

    /**
     * 差异明细
     */
    private String diffDetail;

    /**
     * 状态（1-待审核 2-已审核）
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

    /**
     * 备注
     */
    private String remark;
}
