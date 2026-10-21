package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 患者端小程序 JSAPI 预下单结果：拿到的收银台参数与失败原因二选一。
 *
 * <p>没有「模拟支付成功」这一态：渠道出不出收银台参数只由渠道说了算，
 * 后端不据此把单据置成已支付，收款结果只来自渠道异步通知或主动查单。
 * 失败时 {@code payParams} 为空、{@code errMsg} 写明原因，绝不误报成功。
 */
@Data
@NoArgsConstructor
public class MiniPayUnifiedResultVO {

    /**
     * 收银台参数：微信为 wx.requestPayment 所需的 timeStamp/nonceStr/package/signType/paySign，
     * 支付宝为收银台表单串（orderInfo）；失败时为 null。
     */
    private Map<String, String> payParams;

    /**
     * 失败原因（成功时为 null）。
     */
    private String errMsg;
}
