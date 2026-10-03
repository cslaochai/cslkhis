package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 慢病档案作废入参
 */
@Data
public class ChronicCancelDTO {
    @NotNull(message = "档案ID不能为空")
    private Long recordId;
}
