package com.his.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 医保审核证据判定入参（G-07）。
 */
@Data
public class InsuranceEvidenceDTO {

    /**
     * 合规审核记录ID
     */
    @NotNull(message = "审核记录ID不能为空")
    private Long auditId;
}
