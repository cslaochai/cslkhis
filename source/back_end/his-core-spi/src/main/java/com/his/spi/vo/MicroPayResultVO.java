package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 付款码收款结果：柜面扫患者付款码这一次渠道请求的成败与渠道交易号。
 */
@Data
@NoArgsConstructor
public class MicroPayResultVO {

    /**
     * 渠道是否确认实收（true-已收到 false-未收到）
     */
    private boolean success;

    /**
     * 渠道交易号（成功时用于与渠道账单勾对）
     */
    private String channelTxnNo;

    /**
     * 失败原因（成功时为空白）
     */
    private String errMsg;
}
