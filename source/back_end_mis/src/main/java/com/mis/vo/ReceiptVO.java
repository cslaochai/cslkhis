package com.mis.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保端报文回执（HIS 端读取 replyPayload 的字段来源）
 */
@Data
public class ReceiptVO {

    /** 0=成功，-1=失败（沿用 HIS mock 前置机口径） */
    private Integer infcode;
    private String warnMsg;
    private String errMsg;
    private String msgType;
    private String tradeNo;
    private String receiptNo;
    private String settlementNo;
    private String replyTime;
    /** 平台核定统筹（2304 有值） */
    private BigDecimal poolConfirm;
    private String note;
}
