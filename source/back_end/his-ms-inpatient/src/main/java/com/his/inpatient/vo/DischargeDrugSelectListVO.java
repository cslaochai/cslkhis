package com.his.inpatient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出院带药下拉出参：选「发哪一行药」需要的列（药名 + 规格 + 用法用量 + 摆药状态）。
 */
@Data
@Schema(name = "DischargeDrugSelectListVO", description = "出院带药下拉出参")
public class DischargeDrugSelectListVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String spec;

    /** 单次剂量 */
    private String dosage;

    /** 单位 */
    private String unit;

    /** 发药数量 */
    private BigDecimal quantity;

    /**
     * 用药医嘱
     */
    private String usageText;

    /**
     * 发药状态（1-待发药 2-已发药）
     */
    private Integer dispenseStatus;

    /** 发药状态文案 */
    private String dispenseStatusText;
}
