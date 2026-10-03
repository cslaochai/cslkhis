package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 渠道流水分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PayChannelQueryPageDTO extends PageParam {

    /**
     * 支付渠道：2-微信 3-支付宝 6-银行卡（对齐支付资金流水的支付方式）
     */
    private Integer channel;

    /**
     * 勾对状态（0-待勾对 1-已勾对 2-长款 3-短款）
     */
    private Integer matchStatus;

    /**
     * 账单日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    /**
     * 本地支付流水号（精确，支付资金流水的流水编号）
     */
    private String localTxnNo;

    /**
     * 渠道流水号（模糊）
     */
    private String channelTradeNo;
}
