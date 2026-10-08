package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 住院医嘱校对完成通知载荷（对应 InpatientOrderServiceImpl 的校对提醒）。
 */
@Data
public class InpatientOrderVerifyNotifyPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 床号（在院患者为空）
     */
    private String bedNo;

    /**
     * 该组首条医嘱号
     */
    private String orderNo;

    /**
     * 本组医嘱条数
     */
    private Integer count;

    /**
     * 校对护士姓名
     */
    private String verifyNurse;
}
