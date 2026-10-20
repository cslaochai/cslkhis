package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 聚合码模拟收款登记信息：一张码被模拟付掉后记下的实际渠道与实收金额。
 */
@Data
@NoArgsConstructor
public class AggPaidInfoVO {

    /**
     * 实际收款渠道（1-微信 2-支付宝 3-云闪付）
     */
    private Integer channel;

    /**
     * 渠道实收金额
     */
    private BigDecimal amount;
}
