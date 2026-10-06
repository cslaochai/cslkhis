package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 结算清单编码明细维护入参（诊断 + 手术操作一次提交）
 *
 * <p>采用整单覆盖语义：提交的列表即该清单的最终编码明细，未提交的会被删除。
 * 之所以不做增量（单条增删改），是因为编码明细是一个整体 ——
 * 主诊断只能有一条，漏删一条旧主诊断会让整份清单的主诊断变成两条，
 * 而审核规则会直接读到这个脏状态。</p>
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
