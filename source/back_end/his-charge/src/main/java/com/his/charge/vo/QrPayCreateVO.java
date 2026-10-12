package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 扫码收款出码结果：图片直接给前端 &lt;img src&gt;，前端不需要知道码串是什么格式。
 */
@Data
public class QrPayCreateVO {

    /**
     * 支付单号（轮询与关单都靠它）
     */
    private String payNo;

    /**
     * 收款渠道（1-微信 2-支付宝）
     */
    private Integer channel;

    /**
     * 本单金额（元）——由服务端按账单实时应缴算出，不是页面传值
     */
    private BigDecimal amount;

    /**
     * 二维码图片（data:image/png;base64,...）
     */
    private String qrImage;

    /**
     * 支付单状态（0-待支付 1-已支付）
     */
    private Integer payStatus;
}
