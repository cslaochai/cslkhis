package com.his.charge.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PayChannelService {

    List<ChannelTrade> fetchChannelBill(Integer channel, LocalDate billDate);

    /**
     * 渠道侧一笔流水。
     */
    public record ChannelTrade(String channelTradeNo, LocalDateTime tradeTime, BigDecimal amount)
            implements Serializable {
    }
}
