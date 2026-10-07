package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 入院收治通知载荷（对应 {@code InpatientServiceImpl#notifyAdmitted}）。
 *
 * <p>收件人是开住院证的开证医生本人：病人已收治到他要有个回音，
 * 否则他会以为住院证还没被处理。床位/病区取实际安排结果，不取住院证上的意向。
 */
@Data
public class InpatientAdmitNotifyPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 住院证号
     */
    private String orderNo;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 住院号
     */
    private String admissionNo;
}
