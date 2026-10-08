package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支付资金流水（L3）：一笔真金白银一行，收退同表带符号（收款正、退款负）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_payment_txn")
public class BizPaymentTxn extends BaseEntity {

    /**
     * 流水号：收款 PT、退款 RT 前缀，看号就知道钱的方向
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
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
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
     * 资金方向，字典 {@code his_pay_direction}：1-收款 2-退款
     */
    private Integer direction;

    /**
     * 支付方式，字典 {@code his_pay_method}（4 仅指医保个人账户，统筹不入流水）
     */
    private Integer payMethod;

    /**
     * 金额：收款为正、退款为负，日结 SUM 即净额
     */
    private BigDecimal amount;

    /**
     * 流水状态，字典 {@code his_pay_txn_status}：1-成功 2-已冲正
     */
    private Integer txnStatus;

    /**
     * 退款/冲正指向的原收款流水ID：一账单多笔时必须说清退的是哪一笔钱
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origTxnId;

    /**
     * 流水来源，字典 {@code his_txn_source}
     */
    private Integer sourceType;

    /**
     * 退费方式，字典 {@code his_refund_method}（仅 direction=2）
     */
    private Integer refundMethod;

    /**
     * 渠道流水号：微信/支付宝 transaction_id、医保报文 trade_no、余额=账户流水号。
     * M7 口子为 SIMU/SIMR 前缀模拟号，不可作资金核对依据。
     */
    private String channelTxnNo;

    /**
     * 收银/退款人员工ID：班结归集主键（姓名列只用于打印，两个人同名会把两个班串在一起）
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
     * 交易归属日（冗余，日结按天走索引聚合）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate txnDate;

    /**
     * 所属交班单ID：班结归集后回填，NULL=还没交班
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
     * 来源退费申请号
     */
    private String applyNo;

    /**
     * 收据号：柜面给患者的纸质收据号，与流水一对一勾对（患者端自助充值无纸票，留空）。
     * 不复用 {@code channelTxnNo} —— 那一列专门记渠道交易号，勾的是渠道账单。
     */
    private String receiptNo;
}
