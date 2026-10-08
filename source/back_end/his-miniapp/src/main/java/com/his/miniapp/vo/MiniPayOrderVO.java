package com.his.miniapp.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 患者端支付下单结果。
 */
@Data
public class MiniPayOrderVO {

    /**
     * 支付单号
     */
    private String payNo;

    /**
     * 支付单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long payOrderId;

    /**
     * 支付状态（0-待支付 1-已支付 2-已关闭 3-已退款）
     */
    private Integer payStatus;

    /**
     * 金额（元）
     */
    private BigDecimal amount;

    /**
     * 真收银台模式下的 wx.requestPayment 参数（timeStamp/nonceStr/package/signType/paySign）；
     * 模式为 null（后端已直接推进支付成功）。
     */
    private Map<String, String> payParams;
}
