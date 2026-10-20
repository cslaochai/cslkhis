package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病例明细行（StatReportAggMapper#cohortCases 一行，报文明细段）。
 */
@Data
public class StatCohortCaseRowVO implements Serializable {

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime admitTime;

    /**
     * 出院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dischargeTime;

    /**
     * 住院日（按行钳 0，脏数据不出负数）
     */
    private BigDecimal losDays;

    /**
     * 主要诊断 ICD 编码（未编码时为「未编码」）
     */
    private String diagnosisCode;

    /**
     * 主要诊断名称（未填写时为「未填」）
     */
    private String diagnosisName;

    /**
     * 主要手术（无手术记录时为「-」）
     */
    private String mainOperation;

    /**
     * 出院结算总额（无账单时为 0）
     */
    private BigDecimal settleAmount;
}