package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按支付单号操作的入参（轮询收款结果、关单共用）。
 */
@Data
public class QrPayQueryDTO {

    /**
     * 支付单号
     */
    @NotBlank(message = "缺少支付单号")
    private String payNo;

    /**
     * 模拟收款落在哪个资金渠道（1-微信 2-支付宝 3-云闪付），仅 mockPaid 用；不传默认微信。
     * 真实收款不需要它 —— 渠道由聚合网关在支付时回填，前端传什么都不作数。
     */
    private Integer channel;
}
