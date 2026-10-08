package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出院记录快照（跨模块裸 SQL：出院记录 + 入院记录 + 患者档案 + 科室）。
 */
@Data
public class DischargeSnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    private String dischargeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private LocalDateTime dischargeTime;

    /**
     * 出院诊断（出院记录没写就退回入院诊断）
     */
    private String diagnosis;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptName;

    private String patientNo;

    private String patientName;

    private String phone;
}