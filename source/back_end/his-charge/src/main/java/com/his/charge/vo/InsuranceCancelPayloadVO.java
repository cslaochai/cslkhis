package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 医保 2305 撤销报文（InsuranceSettlementServiceImpl#sendCancel）。
 */
@Data
public class InsuranceCancelPayloadVO implements Serializable {

    /**
     * 报文类型，固定 2305（撤销）
     */
    private String msgType;

    /**
     * 本次交易流水号
     */
    private String tradeNo;

    /**
     * 被撤销的上传交易流水号
     */
    private String origTradeNo;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 账单号
     */
    private String billNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 撤销原因
     */
    private String cancelReason;

    /**
     * 发送时间（{@code yyyy-MM-dd HH:mm:ss}）
     */
    private String sendTime;

    /**
     * 备注（G7 样例报文说明）
     */
    private String note;
}