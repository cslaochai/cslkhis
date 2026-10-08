package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * DRG 分组模拟的输入行：病案首页 + 实际费用（{@code DrgSimMapper#selectSummary} /
 * {@code selectSummaries} 一行，单条模拟与批量模拟共用同一形状）。
 *
 * <p>费用用标量子查询而不是 LEFT JOIN 结算账单表：一次住院除了出院结算还可能有中途结算账单，
 * join 会把一行首页放大成多行，DRG 模拟就凭空多出几个病例。首页快照优先，
 * 没有再看出院结算账单，都取不到按 0 计。
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