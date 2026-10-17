package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 欠费提醒站内信的报文载荷（InpatientAccountServiceImpl#notifyArrears）。
 */
@Data
public class ArrearsNoticePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 欠费金额（元，字符串形态）
     */
    private String arrears;

    /**
     * 住院账户余额（元，字符串形态）
     */
    private String balance;

    /**
     * 触发场景（如"入院登记" / "出院结算"）
     */
    private String scene;
}