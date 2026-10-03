package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院出院结算出参（<b>读的是 L2 那张 {@code bill_type=4} 账单 + L3 的资金事实</b>）。
 *
 * <p>旧表住院结算单那份"结算台账"已退役：同一件事不许有两份记录，
 * 两份记录必然漂移，出院门禁就会读到过期那一份。所以这里没有一张"结算单表"，
 * 每张字段都能指回账单列或支付流水：
 * <ul>
 *   <li>{@code settlementNo} = 账单号；{@code totalAmount/poolAmount/payableAmount} = 账单列。</li>
 *   <li>{@code paidAmount} = 账单镜像（权威在流水），{@code balanceUsed}/{@code refundAmount}
 *       是按流水现算的两个资金事实（抵扣了多少、退差多少）。</li>
 *   <li>{@code settleStatus} 是<b>派生值</b>（{@code payable − paid} 是否为正），不落列、不刷状态。</li>
 * </ul>
 * 已作废的账单不出现在这里（"作废=这次结算撤销、要重结"，它不算结算）。
 */
@Data
public class InpatientSettlementVO implements Serializable {

    /**
     * 结算账单ID（L2）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算单号（= 出院结算账单号）
     */
    private String settlementNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 本次结算纳入的记账行数
     */
    private Integer feeCount;

    /**
     * 应收合计
     */
    private BigDecimal totalAmount;

    /**
     * 院内优惠/抹零
     */
    private BigDecimal discountAmount;

    /**
     * 医保统筹（后付给医保局，不经收银台）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户（刷参保人卡扣的真实收款）
     */
    private BigDecimal accountAmount;

    /**
     * 患者自付（应收 − 统筹，未减优惠与个账）
     */
    private BigDecimal selfAmount;

    /**
     * 应缴合计（优惠/统筹/个账都扣掉后该收的钱）
     */
    private BigDecimal payableAmount;

    /**
     * 已收合计（账单镜像，权威在支付流水）
     */
    private BigDecimal paidAmount;

    /**
     * 本次从住院账户（预交金）抵扣掉的钱
     */
    private BigDecimal balanceUsed;

    /**
     * 出院退差（住院账户剩下的钱转入患者院内余额，不是现金流出）
     */
    private BigDecimal refundAmount;

    /**
     * 欠费金额（应缴 − 已收，不欠为 0）
     */
    private BigDecimal arrearsAmount;

    /**
     * 结算状态（1-已结清 2-欠费 3-已作废）
     */
    private Integer settleStatus;

    /**
     * 结算状态文案
     */
    private String settleStatusText;

    /**
     * 结算方式：1-自费 2-医保
     */
    private Integer settleMode;

    /**
     * 结算方式文案
     */
    private String settleModeText;

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 结算时间（= 账单出账时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settleTime;

    /**
     * 结算人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settleBy;

    /**
     * 结算人姓名
     */
    private String settleByName;

    /**
     * 备注
     */
    private String remark;
}
