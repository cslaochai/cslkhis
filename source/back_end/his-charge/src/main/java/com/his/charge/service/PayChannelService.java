package com.his.charge.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PayChannelService {

    /**
     * 渠道侧一笔流水。
     *
     * @param channelTradeNo 渠道流水号（勾对时比对支付资金流水的渠道流水编号）
     * @param tradeTime      渠道侧交易时间
     * @param amount         带符号金额：收款为正、退款为负，与本地流水同口径才能直接比
     */
    public record ChannelTrade(String channelTradeNo, LocalDateTime tradeTime, BigDecimal amount)
            implements Serializable {
    }

    List<ChannelTrade> fetchChannelBill(Integer channel, LocalDate billDate);
}
