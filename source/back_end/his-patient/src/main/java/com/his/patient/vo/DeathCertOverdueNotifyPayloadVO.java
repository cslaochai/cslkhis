package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 死亡证明逾期未上报催报载荷（对应 DeathCertificateServiceImpl#notifyOverdue）。
 */
@Data
public class DeathCertOverdueNotifyPayloadVO implements Serializable {

    /**
     * 死亡证明ID
     */
    private String certId;

    /**
     * 死亡证明编号
     */
    private String certNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 上报时限
     */
    private String deadline;

    /**
     * 已逾期天数
     */
    private Long lateDays;
}
