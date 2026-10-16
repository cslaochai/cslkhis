package com.his.api.vo;

import lombok.Data;

/**
 * 入院单快照（读侧契约：住院状态与就诊科室/病区/床位归属）。
 */
@Data
public class AdmissionVO {

    private Long admissionId;

    private String admissionNo;

    private Long patientId;

    private Long deptId;

    private Long wardId;

    private Long bedId;

    /**
     * 住院状态（com.his.common.enums.AdmitStatusEnum）
     */
    private Integer admitStatus;
}
