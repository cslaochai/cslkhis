package com.his.charge.dto;

import com.his.common.enums.PayChannelEnum;
import com.his.common.validation.InEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 扫码收款出码入参（医院出码、患者用手机扫）。
 */
@Data
public class QrPayCreateDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;

    /**
     * 收款渠道（1-微信 2-支付宝）。
     * 一码只属于一个渠道：微信扫不开支付宝的收款码，所以必须先定渠道再生成码，不存在「通用码」。
     */
    @NotNull(message = "缺少收款渠道")
    @InEnum(value = PayChannelEnum.class, message = "收款渠道不合法（1-微信 2-支付宝）")
    private Integer channel;
}
