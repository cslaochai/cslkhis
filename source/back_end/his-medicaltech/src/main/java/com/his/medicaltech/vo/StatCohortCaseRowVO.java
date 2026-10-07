package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 病例明细行（{@code StatReportAggMapper#cohortCases} 一行，报文明细段）。
 *
 * <p>500 条封顶：报文一旦无上限就会把整个payload 撑到几百 KB 落库，
 * 台账表体积和打印页数都会失控。
 *
 * <p>入出院时间用 {@link LocalDateTime} 接收、由 {@link JsonFormat} 定输出格式，
 * 而不是 SQL 里 {@code DATE_FORMAT} 成字符串 —— 换VO 后格式这件事归VO 管，
 * 不用改 SQL；输出仍是 {@code yyyy-MM-dd HH:mm}，前端打印与页面表格不变。
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