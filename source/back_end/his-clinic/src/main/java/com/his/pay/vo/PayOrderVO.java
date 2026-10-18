package com.his.pay.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单快照（跨模块只透出这个，不透出实体）。
 */
@Data
public class PayOrderVO {

    private Long id;

    private String payNo;

    /**
     * 业务类型（1-门诊缴费 2-挂号费 3-住院押金）
     */
    private Integer bizType;

    private Long bizId;

    /** 患者ID */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    private BigDecimal amount;

    /**
     * 支付渠道（1-微信 2-支付宝 3-云闪付）
     */
    private Integer channel;

    /**
     * 支付状态（0-待支付 1-已支付 2-已关闭 3-已退款）
     */
    private Integer payStatus;

    private String outTradeNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /**
     * 支付码过期时刻（服务端按创建时间 + 有效期现算，库里不存这份派生值）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 出码人（渠道回调是无登录态的系统入口，收款流水的收银员归属只能取自这里，不能取自当时的登录人）
     */
    private Long cashierId;

    private String cashierName;
}
