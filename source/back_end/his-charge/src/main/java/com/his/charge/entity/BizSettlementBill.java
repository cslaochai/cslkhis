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
 * 结算账单（L2）：把若干条记账行锁定成"这一笔该收多少、怎么分"。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement_bill")
public class BizSettlementBill extends BaseEntity {

    /**
     * 账单号
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
     * 就诊类型（1-门诊 2-住院），字典 {@code his_encounter_type}
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 就诊标识单号
     */
    private String encounterNo;

    /**
     * 账单类型，字典 {@code his_bill_type}：1-挂号费 2-门诊诊间 3-住院中途 4-出院结算
     */
    private Integer billType;

    /**
     * 纳入本账单的记账行数
     */
    private Integer feeCount;

    /**
     * 应收合计（Σ账单行 amount，医保前总价）
     */
    private BigDecimal totalAmount;

    /**
     * 院内优惠/抹零。只放真优惠 —— 医保统筹绝不再写这里（旧实现把统筹塞进"优惠金额"列，
     * 结果真优惠无处安放，读取侧只能 insurancePay = discountAmount 猜）。
     */
    private BigDecimal discountAmount;

    /**
     * 结算方式，字典 {@code his_settlement_mode}：1-自费 2-医保
     */
    private Integer settlementMode;

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 医保统筹支付：后付给医保局的钱，不进本院现金清点，日结单列一栏与报盘对账。
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户支付（刷参保人卡扣的额度，是一笔真实收款流水）
     */
    private BigDecimal accountAmount;

    /**
     * 个人自费
     */
    private BigDecimal selfAmount;

    /**
     * 患者应缴 = total - discount - pool - account
     */
    private BigDecimal payableAmount;

    /**
     * 已收合计（冗余值，权威在支付资金流水）
     */
    private BigDecimal paidAmount;

    /**
     * 本账单已退合计
     */
    private BigDecimal refundAmount;

    /**
     * 账单状态，字典 {@code his_bill_status}：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费
     */
    private Integer billStatus;

    /**
     * 账务归属日：日结按它聚合，不按 create_time
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 结算生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billTime;

    /**
     * 结算人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billById;

    /**
     * 结算人姓名
     */
    private String billByName;

    /**
     * 收讫时间（最后一笔成功收款流水；0 元单免收核销时也写）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 作废操作人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voidById;

    /**
     * 作废操作人姓名
     */
    private String voidByName;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;

    /**
     * 作废原因（必填）
     */
    private String voidReason;

    /**
     * 红冲指针：本账单作废重结时指向被冲的原账单
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origBillId;

    /**
     * 结清说明：免收核销/挂账等特殊结清路径写这里，避免"没流水却已支付"看不出原因
     */
    private String closeReason;
}
