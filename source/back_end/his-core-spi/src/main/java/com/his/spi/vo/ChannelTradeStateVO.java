package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 渠道交易状态快照：主动查单拿回的一笔渠道侧事实。
 */
@Data
@NoArgsConstructor
public class ChannelTradeStateVO {

    /**
     * 渠道交易号（原路退款与对账勾对靠它）
     */
    private String tradeNo;

    /**
     * 渠道交易状态原文（渠道协议字段，不是本库码值）
     */
    private String tradeStatus;

    /**
     * 渠道侧订单金额，用于与本地支付单逐字比对
     */
    private BigDecimal totalAmount;
}
