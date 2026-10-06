package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退费申请出参
 */
@Data
public class BizRefundApplyVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 退费申请单号
     */
    private String refundApplyNo;

    /**
     * 原结算账单ID（L2）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    /**
     * 结算账单号
     */
    private String billNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 退费类型（1-退药 2-退检查 3-退检验 4-退治疗 5-全部退费）
     */
    private Integer refundType;

    /**
     * 退费原因
     */
    private String refundReason;

    /**
     * 退费金额，单位：元
     */
    private BigDecimal refundAmount;

    /**
     * 申请状态（1-待审核 2-审核通过 3-审核驳回 4-已退费 5-已作废）
     */
    private Integer applyStatus;

    /**
     * 申请人
     */
    private String applyBy;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 退费执行人
     */
    private String refundBy;

    /**
     * 退费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * 作废人（5-已作废时非空）
     */
    private String cancelBy;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 作废原因
     */
    private String cancelReason;

    // 退费流水（L3 支付资金流水，direction=2 且 apply_id=本单）：只有执行成功才非空
    // 一笔申请可能拆成多笔原路退回（现金 40 + 微信 150 + 余额 110），所以下面既有"第一笔"的口径，
    // 也有笔数与合计；把多笔挤成一个退费单号等于把退出去的钱说小。

    /**
     * 退款流水笔数
     */
    private Integer flowTxnCount;

    /**
     * 退款合计（绝对值，元）
     */
    private BigDecimal flowRefundAmount;

    /**
     * 退款流水号（多笔时为首笔，全部笔数见 {@code flowTxnCount}）
     */
    private String flowRefundNo;

    /**
     * 退费方式（字典 {@code his_refund_method}：1-原路退回 2-现金退回 3-余额退回）
     */
    private Integer flowRefundMethod;

    /**
     * 原支付方式（字典 {@code his_pay_method}）
     */
    private Integer flowPayMethod;

    /**
     * 渠道退费流水号（M7 口子为 SIMR- 前缀模拟号）
     */
    private String flowChannelRefundNo;

    /**
     * 医保报盘是否随本次退费撤销（1-已发 2305）
     */
    private Integer flowInsuranceCancelled;

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
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
