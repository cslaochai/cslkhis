package com.his.drgdip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可模拟病案首页列表一行（DipSimMapper#summaryList）。
 */
@Data
public class DipSummaryListRowVO implements Serializable {

    /**
     * 病案首页ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long summaryId;

    /**
     * 患者姓名（首页快照）
     */
    private String patientName;

    /**
     * 科室名称（首页快照）
     */
    private String deptName;

    /**
     * 出院时间
     */
    private LocalDateTime dischargeTime;

    /**
     * 主要诊断 ICD 编码
     */
    private String mainDiagnosisCode;

    /**
     * 主要诊断名称
     */
    private String mainDiagnosisName;

    /**
     * 是否手术（0-否 1-是）
     */
    private Integer isSurgery;

    /**
     * 已有的模拟结果病种编码（尚未模拟时为 null）
     */
    private String dipCode;

    /**
     * 已有的模拟结果盈亏（尚未模拟时为 null）
     */
    private BigDecimal profitAmount;
}
