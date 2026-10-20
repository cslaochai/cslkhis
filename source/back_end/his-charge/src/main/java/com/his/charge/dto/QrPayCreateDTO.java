package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聚合支付码出码入参（医院出一张聚合码，患者用手机扫）。
 *
 * <p>聚合码不绑定渠道：同一张码微信、支付宝、云闪付都能扫，渠道在患者支付那一刻由医院聚合网关
 * 识别运行环境后确定。所以入参里<b>没有渠道</b>——先选渠道再出码是「一码一渠道」时代的做法，
 * 那正是「从支付宝切到微信要重出一张码」的成因。
 */
@Data
public class QrPayCreateDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;
}
