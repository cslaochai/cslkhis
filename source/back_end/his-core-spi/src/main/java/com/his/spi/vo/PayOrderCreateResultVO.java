package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建单结果：本次拿到的支付单，以及它是不是复用回来的原单。
 */
@Data
@NoArgsConstructor
public class PayOrderCreateResultVO {

    /**
     * 待支付单快照（新建的或复用的原单）
     */
    private PayOrderInfoVO order;

    /**
     * 是否命中原有可复用待支付单（true-没有新建也没关单，渠道侧那张码还在、码串没变）
     */
    private boolean reused;
}
