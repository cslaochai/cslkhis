package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 追溯码作废入参（退药 / 报损 / 召回）
 */
@Data
public class DrugTraceVoidDTO {

    @NotNull(message = "追溯码记录ID不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long traceId;

    /** 作废类型（1-退药 2-报损 3-召回） */
    @NotNull(message = "作废类型不能为空")
    private Integer voidType;

    /** 作废原因 */
    private String reason;
}
