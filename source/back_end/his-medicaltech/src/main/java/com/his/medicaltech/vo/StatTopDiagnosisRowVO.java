package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 主要诊断顺位（StatReportAggMapper#topDiagnoses 一行）。
 */
@Data
public class StatTopDiagnosisRowVO implements Serializable {

    /**
     * ICD 编码（未编码时为「未编码」）
     */
    private String diagnosisCode;

    /**
     * 诊断名称（未填写时为「未填」）
     */
    private String diagnosisName;

    /**
     * 例数
     */
    private Long cnt;
}