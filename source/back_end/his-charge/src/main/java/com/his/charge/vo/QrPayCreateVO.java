package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * 支付码过期时刻
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 服务端当前时间：前端用「expireTime - serverTime」锚定倒计时，客户端时钟不准也不会把新码显示成已过期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serverTime;
}
