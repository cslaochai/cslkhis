package com.his.app.vo;

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
     * 真收银台下的渠道参数：微信为 wx.requestPayment 所需的
     * timeStamp/nonceStr/package/signType/paySign，支付宝为 form 表单串。
     * 后端不替渠道把单据置成已支付，收款结果只来自渠道异步通知或主动查单。
     */
    private Map<String, String> payParams;
}
