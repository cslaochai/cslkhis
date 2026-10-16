package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退费流水出参（列表与「退费管理」回显共用）
 */
@Data
public class BizRefundFlowVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 退费单号
     */
    private String refundNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long chargeId;

    /**
     * 原收费单号
     */
    private String chargeNo;

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
     * 退费类型（字典 his_refund_apply_type）
     */
    private Integer refundType;

    /**
     * 本次实退金额
     */
    private BigDecimal totalAmount;

    private String refundReason;

    /**
     * 退费状态（字典 his_refund_status），新写入一律 3-已退费
     */
    private Integer refundStatus;

    /**
     * 退费方式（字典 his_refund_method：1-原路退回 2-现金退回 3-余额退回）
     */
    private Integer refundMethod;

    /**
     * 原支付方式（字典 his_pay_method）
     */
    private Integer payMethod;

    /**
     * 渠道退费流水号（M7 口子下为 SIMR- 前缀模拟号）
     */
    private String channelRefundNo;

    /**
     * 流水来源（字典 his_refund_flow_source）
     */
    private Integer flowSource;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 来源退费申请号
     */
    private String applyNo;

    /**
     * 医保报盘是否已随本次退费撤销（0-不涉及 1-已发 2305）
     */
    private Integer insuranceCancelled;

    /**
     * 退费操作人
     */
    private String refundBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
