package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 单病种自动扫描候选：已出院 + 有首页 + 主要诊断命中病种 ICD 前缀 + 尚未纳入该病种。
 */
@Data
public class SingleDiseaseCandidateVO implements Serializable {

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /**
     * 主要诊断 ICD-10 码（命中扫描前缀的那个）
     */
    private String mainDiagnosisCode;

    /** 主要诊断名称（与 mainDiagnosisCode 同一诊断） */
    private String mainDiagnosisName;
}