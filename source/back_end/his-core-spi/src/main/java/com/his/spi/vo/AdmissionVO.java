package com.his.spi.vo;

import lombok.Data;

/**
 * 入院单快照（读侧契约：住院状态与就诊科室/病区/床位归属）。
 */
@Data
public class AdmissionVO {

    /** 入院ID */
    private Long admissionId;

    /** 住院号 */
    private String admissionNo;

    /** 患者ID */
    private Long patientId;

    /** 就诊科室ID */
    private Long deptId;

    /** 病区ID */
    private Long wardId;

    /** 床位ID */
    private Long bedId;

    /**
     * 住院状态（com.his.common.enums.AdmitStatusEnum）
     */
    private Integer admitStatus;
}
