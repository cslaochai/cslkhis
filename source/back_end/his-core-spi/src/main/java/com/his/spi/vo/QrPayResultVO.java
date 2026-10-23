package com.his.spi.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 聚合支付出码结果（结诊文书左上角二维码渲染用）。
 * 图片直接给渲染端 &lt;img src&gt;，调用方不需要知道码串是什么格式。
 */
@Data
public class QrPayResultVO {

    /**
     * 支付单号（轮询与关单都靠它）
     */
    private String payNo;

    /**
     * 账单号（核对基准：出码目标是哪张单）
     */
    private String billNo;

    /**
     * 本单金额（元）——由服务端按账单实时应缴算出
     */
    private BigDecimal amount;

    /**
     * 二维码图片（data:image/png;base64,...）
     */
    private String qrImage;

    /**
     * 支付码过期时刻
     */
    private LocalDateTime expireTime;

    /**
     * 服务端当前时间（前端锚定倒计时）
     */
    private LocalDateTime serverTime;
}
