package com.his.drgdip.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DIP 分组模拟的输入行：病案首页的分组维度 + 实际费用（同 DRG 模拟输入，DIP 多取手术操作编码）。
 */
@Data
public class DipSummaryRowVO implements Serializable {

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
     * 是否手术（0-否 1-是）：结果行快照，入组判定看手术编码本身
     */
    private Integer isSurgery;

    /**
     * 住院天数：结果行快照
     */
    private Integer inpatientDays;

    /**
     * 实际住院费用
     */
    private BigDecimal actualAmount;

    /**
     * 入院ID（关联手术/诊断明细）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄数值，配合 ageUnit
     */
    private Integer age;

    /**
     * 年龄单位（1-岁 2-月 3-天）：决定能不能换算周岁参与年龄型辅助目录
     */
    private Integer ageUnit;

    /**
     * 出生体重（克）：DIP 不参与分组判定，保留对齐 DRG 输入结构
     */
    private Integer birthWeight;
}
