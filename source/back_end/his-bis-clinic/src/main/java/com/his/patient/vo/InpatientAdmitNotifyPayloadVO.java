package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 入院收治通知载荷（对应 InpatientServiceImpl#notifyAdmitted）。
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
