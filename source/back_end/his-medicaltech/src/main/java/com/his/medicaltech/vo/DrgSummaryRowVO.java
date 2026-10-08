package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * DRG 分组模拟的输入行：病案首页 + 实际费用（DrgSimMapper#selectSummary /
 */
@Data
public class DrgSummaryRowVO implements Serializable {

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
     * 主要诊断 ICD 编码（模拟时可由入参覆盖，为空则无法入组）
     */
    private String mainDiagnosisCode;

    /**
     * 主要诊断名称（首页快照）
     */
    private String mainDiagnosisName;

    /**
     * 是否手术（0-否 1-是）：分组器据此选手术组/非手术组候选
     */
    private Integer isSurgery;

    /**
     * 住院天数：非手术组内>=10 天进伴并发症档
     */
    private Integer inpatientDays;

    /**
     * 死亡标志（0-否 1-是）：死亡直接进伴并发症档
     */
    private Integer deathFlag;

    /**
     * 实际住院费用
     */
    private BigDecimal actualAmount;
}