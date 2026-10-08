package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单（患者端小程序统一支付凭证，微信支付口子的落库形态）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pay_order")
public class BizPayOrder extends BaseEntity {

    /** 支付单号（Pay+yyyyMMdd+序号，唯一） */
    private String payNo;

    /** 业务类型（1-门诊缴费 2-挂号费 3-住院押金） */
    private Integer bizType;

    /** 业务单ID（收费单ID / 挂号单ID / 入院ID） */
    private Long bizId;

    /** 患者ID */
    private Long patientId;

    /** 患者姓名（冗余，对账用） */
    private String patientName;

    /** 金额（元） */
    private BigDecimal amount;

    /** 支付渠道：1-微信小程序 */
    private Integer channel;

    /** 支付状态（0-待支付 1-已支付 2-已关闭 3-已退款） */
    private Integer payStatus;

    /** 渠道交易号（微信 transaction_id，模式为 MOCK_ 前缀） */
    private String outTradeNo;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /** 退款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;
}
