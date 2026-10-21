package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 入院登记跨域只读摘要：管床/责任护士关系建立时校验在院状态与科室、落关系行快照的最小字段集。
 */
@Data
@NoArgsConstructor
public class AdmissionActiveBriefVO {

    /**
     * 入院ID（住院登记主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 当前科室ID（随转科更新）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 入院时间
     */
    private LocalDateTime admitTime;

    /**
     * 状态（0-已出院 1-在院）
     */
    private Integer admitStatus;
}
