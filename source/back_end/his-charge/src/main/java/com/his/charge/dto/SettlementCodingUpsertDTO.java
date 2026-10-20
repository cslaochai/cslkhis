package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 结算清单编码明细维护入参（诊断 + 手术操作一次提交）
 */
@Data
public class SettlementCodingUpsertDTO {

    /**
     * 结算清单ID
     */
    @NotNull(message = "结算清单ID不能为空")
    private Long settlementId;

    /**
     * 诊断明细（整单覆盖）
     */
    private List<SettlementDiagnosisUpsertDTO> diagnoses;

    /**
     * 手术操作明细（整单覆盖）
     */
    private List<SettlementOperationUpsertDTO> operations;

    /**
     * 备注
     */
    private String remark;
}
