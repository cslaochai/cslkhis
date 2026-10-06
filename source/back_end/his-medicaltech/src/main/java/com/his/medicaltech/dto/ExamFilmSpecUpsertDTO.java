package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 胶片规格价目入参（sql/138）。
 */
@Data
public class ExamFilmSpecUpsertDTO {

    /**
     * 规格ID（新增为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 规格编码
     */
    @NotBlank(message = "缺少规格编码")
    @Size(max = 32, message = "规格编码不能超过 32 字")
    private String specCode;

    /**
     * 规格名称
     */
    @NotBlank(message = "缺少规格名称")
    @Size(max = 100, message = "规格名称不能超过 100 字")
    private String specName;

    /**
     * 单价
     */
    @NotNull(message = "缺少单价")
    @DecimalMin(value = "0", message = "单价不能为负")
    private BigDecimal unitPrice;

    /**
     * 单位
     */
    @Size(max = 20, message = "单位不能超过 20 字")
    private String unit;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
