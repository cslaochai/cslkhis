package com.his.appoint.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 急诊留观超时限催办的站内信业务上下文（随站内信下发）。
 */
@Data
public class EmergencyObsTimeoutPayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 急诊单号
     */
    private String emergencyNo;

    /**
     * 急诊科名称
     */
    private String deptName;

    /**
     * 已留观小时数
     */
    private Long obsHours;

    /**
     * 占用的留观床号（未登记时为 null）
     */
    private String observationBed;
}
