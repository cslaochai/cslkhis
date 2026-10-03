package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付资金流水出参（L3 台账）。
 *
 * <p>金额带符号：收款为正、退款为负，日结与班结直接 {@code SUM(amount)} 即净额。
 */
@Data
public class BizPaymentTxnVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 流水号（收=PT、退=RT 前缀，号本身就带方向）
     */
    private String txnNo;

    /**
     * 结算账单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 账单号快照
     */
    private String billNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号（快照）
     */
    private String patientNo;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 就诊类型
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 资金方向（字典 his_pay_direction：1-收款 2-退款）
     */
    private Integer direction;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）
     */
    private Integer payMethod;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 流水状态（字典 his_pay_txn_status：1-成功 2-已冲正）
     */
    private Integer txnStatus;

    /**
     * 退款/冲正指向的原收款流水ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origTxnId;

    /**
     * 流水来源（字典 his_txn_source）
     */
    private Integer sourceType;

    /**
     * 退费方式（字典 his_refund_method：1-原路退回 2-现金退回 3-余额退回）
     */
    private Integer refundMethod;

    /**
     * 渠道流水号（微信/支付宝/银行卡交易号；模拟口子为 SIMU/SIMR 前缀）
     */
    private String channelTxnNo;

    /**
     * 收银/退款人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cashierId;

    /**
     * 操作人姓名
     */
    private String cashierName;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime txnTime;

    /**
     * 交易归属日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate txnDate;

    /**
     * 所属交班单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cashierSettlementId;

    /**
     * 本次退费是否已撤销医保报盘（0-不涉及 1-已发 2305）
     */
    private Integer insuranceCancelled;

    /**
     * 退款/冲正原因
     */
    private String reason;

    /**
     * 来源退费申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 来源退费申请号（快照）
     */
    private String applyNo;

    /**
     * 收据号：柜面给患者的纸质收据号，与流水一对一勾对
     */
    private String receiptNo;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
