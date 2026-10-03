package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端费用解释入参。
 * <p>
 * 同 {@link PatientReportExplainDTO} 的理由：只收 billId，患者身份服务端取。
 */
@Data
@Schema(description = "患者端费用解释入参")
public class PatientFeeExplainDTO {

    @NotBlank(message = "billId不能为空")
    @Schema(description = "结算账单ID", example = "2103793966972424200")
    private String billId;
}
