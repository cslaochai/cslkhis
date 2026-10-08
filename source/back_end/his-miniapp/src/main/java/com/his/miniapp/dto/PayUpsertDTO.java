package com.his.miniapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 患者端支付下单入参。
 */
@Data
public class PayUpsertDTO {

    /**
     * 业务类型（1-门诊缴费 2-挂号费 3-住院押金）
     */
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    /**
     * 业务单ID（收费单ID / 挂号单ID / 入院ID）
     */
    @NotNull(message = "业务单ID不能为空")
    private Long bizId;

    /**
     * 金额（元）。门诊缴费以收费单实收为准忽略该值；挂号费/押金必传
     */
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;
}
