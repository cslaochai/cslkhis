package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人工勾对入参：渠道流水 → 本地支付流水
 */
@Data
public class PayChannelMatchDTO {

    /**
     * 渠道流水台账ID
     */
    @NotNull(message = "台账行不能为空")
    private Long id;

    /**
     * 本地支付流水号（支付资金流水的流水编号）
     */
    @NotBlank(message = "本地支付流水号不能为空")
    private String localTxnNo;
}
