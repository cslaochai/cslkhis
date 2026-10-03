package com.his.pharmacy.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 追溯码作废入参（退药 / 报损 / 召回）
 *
 * <p>发出去再退回的药不得再销售，所以这里没有"回库重新在库"的选项，一律置已作废，
 * 且作废本身也是要上传医保局的变更（upload_status 重置为待上传）。
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
