package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 建待支付单入参：一张收款码背后的业务单、金额与渠道。
 *
 * <p>内部指令而非 HTTP 入参，字段校验由调用方负责（金额必须按账单实时应缴现算后传入）。
 */
@Data
@NoArgsConstructor
public class PayOrderCreateDTO {

    /**
     * 业务类型（1-门诊缴费 2-挂号费 3-住院押金）
     */
    private Integer bizType;

    /**
     * 业务单ID（账单ID / 挂号单ID / 入院ID）
     */
    private Long bizId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者姓名快照
     */
    private String patientName;

    /**
     * 本单应收金额，必须大于 0
     */
    private BigDecimal amount;

    /**
     * 支付渠道（1-微信 2-支付宝 3-云闪付）；聚合码出码时渠道尚未确定，传 null
     */
    private Integer channel;

    /**
     * 建单说明
     */
    private String remark;
}
