package com.his.patient.vo;

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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    private String drugName;

    private String spec;

    private String dosage;

    private String unit;

    private BigDecimal quantity;

    /**
     * 用药医嘱
     */
    private String usageText;

    /**
     * 发药状态（1-待发药 2-已发药）
     */
    private Integer dispenseStatus;

    private String dispenseStatusText;
}
