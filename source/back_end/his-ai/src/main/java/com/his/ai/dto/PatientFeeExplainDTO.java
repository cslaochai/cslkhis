package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端费用解释入参。
 */
@Data
@Schema(description = "患者端费用解释入参")
public class PatientFeeExplainDTO {

    @NotNull(message = "billId不能为空")
    @Schema(description = "结算账单ID", example = "2103793966972424200")
    private Long billId;
}
