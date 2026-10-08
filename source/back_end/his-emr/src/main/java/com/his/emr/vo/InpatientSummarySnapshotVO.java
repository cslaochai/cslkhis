package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 病案首页取数快照（单病种纳入时服务端重查）。
 */
@Data
public class InpatientSummarySnapshotVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientName;

    /**
     * 主要诊断 ICD-10 码
     */
    private String mainDiagnosisCode;

    private String mainDiagnosisName;

    /**
     * 住院天数
     */
    private Integer inpatientDays;

    /**
     * 总费用（元）
     */
    private BigDecimal totalAmount;

    /**
     * 是否有手术（1-有 0-无）
     */
    private Integer isSurgery;

    /**
     * 死亡标志（1-死亡）
     */
    private Integer deathFlag;
}