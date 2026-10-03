package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结算账单出参（L2 台账列表）。字典码值文案由前端字典渲染。
 */
@Data
public class BizSettlementBillVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 账单号（SB+yyyyMMdd+5位）
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
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 就诊标识单号（快照）
     */
    private String encounterNo;

    /**
     * 账单类型（1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）
     */
    private Integer billType;

    /**
     * 纳入本账单的记账行数
     */
    private Integer feeCount;

    /**
     * 应收合计（医保前总价）
     */
    private BigDecimal totalAmount;

    /**
     * 院内优惠/抹零（不含医保统筹）
     */
    private BigDecimal discountAmount;

    /**
     * 结算方式（字典 his_settlement_mode：1-自费 2-医保）
     */
    private Integer settlementMode;

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 医保统筹（后付给医保局，不进现金清点）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户支付
     */
    private BigDecimal accountAmount;

    /**
     * 个人自付（= payable_amount 口径）
     */
    private BigDecimal selfAmount;

    /**
     * 患者应缴 = total - discount - pool - account
     */
    private BigDecimal payableAmount;

    /**
     * 已收合计（冗余值，权威在支付流水）
     */
    private BigDecimal paidAmount;

    /**
     * 本账单已退合计
     */
    private BigDecimal refundAmount;

    /**
     * 账单状态（字典 his_bill_status：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）
     */
    private Integer billStatus;

    /**
     * 账务归属日
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
     * 结算人姓名（快照）
     */
    private String billByName;

    /**
     * 收讫时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 结清说明（免收核销/挂账等特殊结清路径在这里留原因，避免"没流水却已支付"看不出所以然）
     */
    private String closeReason;

    /**
     * 作废原因（必填）
     */
    private String voidReason;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;

    /**
     * 作废操作人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long voidById;

    /**
     * 作废操作人姓名（快照）
     */
    private String voidByName;

    /**
     * 红冲指针：本账单作废重结时指向被冲的原账单
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origBillId;

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

    /**
     * 尚需缴 = 应缴 -(已收 - 已退)，现算，列表页"还欠多少"只有这一个口径
     */
    private BigDecimal unpaidAmount;
}
