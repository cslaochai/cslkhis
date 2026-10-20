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
     * 账单号（核对基准：出码目标是哪张单，以服务端按 billId 查到的事实为准。
     * 前端必须用本字段覆盖列表行旧值显示——传错 billId 时账单号当场对不上）
     */
    private String billNo;

    /**
     * 患者姓名（核对基准：同上。防「列表行与出码目标错位」的静默事故——
     * 收银员盯着 A 患者的信息、手里是 B 患者的码，全靠这一字段暴露）
     */
    private String patientName;

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
