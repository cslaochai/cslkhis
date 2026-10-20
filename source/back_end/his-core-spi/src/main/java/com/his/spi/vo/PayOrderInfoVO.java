package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单快照：收款台与患者端要知道的一笔线上收款的身份、金额与状态。
 */
@Data
@NoArgsConstructor
public class PayOrderInfoVO {

    /**
     * 订单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 平台支付单号
     */
    private String payNo;

    /**
     * 业务类型（线上收款挂的业务单据类型）
     */
    private Integer bizType;

    /**
     * 业务单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bizId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名快照
     */
    private String patientName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道
     */
    private Integer channel;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 渠道侧交易号
     */
    private String outTradeNo;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 订单失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 收银员员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long cashierId;

    /**
     * 收银员姓名
     */
    private String cashierName;
}
