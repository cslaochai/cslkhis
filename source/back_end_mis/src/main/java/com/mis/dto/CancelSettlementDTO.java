package com.mis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 2305 结算撤销报文（字段与 HIS 报盘口径一致）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelSettlementDTO {

    private String msgType;
    private String tradeNo;
    /** 被撤销的原上传流水号 */
    private String origTradeNo;
    private String settlementNo;
    private String billNo;
    private String patientName;
    private String cancelReason;
    private String sendTime;
}
