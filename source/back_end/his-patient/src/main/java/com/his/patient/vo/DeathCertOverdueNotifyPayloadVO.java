package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 死亡证明逾期未上报催报载荷（对应 {@code DeathCertificateServiceImpl#notifyOverdue}）。
 *
 * <p>催报正文已经把关键信息写成自然语言，payload 只带结构化字段供收件箱摘要/后续跳转取用；
 * 字段名与原 Map 的键一致。
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
