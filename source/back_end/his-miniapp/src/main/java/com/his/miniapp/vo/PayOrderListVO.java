package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 患者端我的支付单列表行。
 */
@Data
public class PayOrderListVO implements Serializable {

    /** 支付单ID（字符串化防 BIGINT 精度丢失） */
    private String id;

    /** 支付单号 */
    private String payNo;

    /** 业务类型（1-门诊缴费 2-挂号费 3-住院押金） */
    private Integer bizType;

    /** 业务单ID（字符串化防 BIGINT 精度丢失） */
    private String bizId;

    /** 金额（元） */
    private BigDecimal amount;

    /** 支付状态（0-待支付 1-已支付 2-已关闭 3-已退款） */
    private Integer payStatus;

    /** 支付时间 */
    private LocalDateTime payTime;
}
