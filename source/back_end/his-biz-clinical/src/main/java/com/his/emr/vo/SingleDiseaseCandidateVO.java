package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 单病种自动扫描候选：已出院 + 有首页 + 主要诊断命中病种 ICD 前缀 + 尚未纳入该病种。
 */
@Data
public class SingleDiseaseCandidateVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientName;

    /**
     * 主要诊断 ICD-10 码（命中扫描前缀的那个）
     */
    private String mainDiagnosisCode;

    private String mainDiagnosisName;
}