package com.his.charge.vo;

import com.his.charge.api.PatientGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 入院记录跨域摘要：收费域只读住院登记的最小字段集。
 */
@Data
@NoArgsConstructor
public class AdmissionBriefVO {

    /**
     * 入院ID（biz_admission 主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 入院时间（账期与日清单的起算点）
     */
    private LocalDateTime admitTime;

    /**
     * 经治医生ID（科室收入归属按主管医生算时要用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDoctorId;

    /**
     * 入院诊断（结算清单主诊断兜底）
     */
    private String diagnosis;
}
