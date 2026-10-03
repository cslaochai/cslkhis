package com.his.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 医保合规审核详情出参（审核主表 + 命中明细 + 编码明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComplianceAuditDetailVO extends ComplianceAuditVO {

    /**
     * 全部规则判定明细（含通过与不适用）
     */
    private List<ComplianceAuditItemVO> items;

    /**
     * 清单诊断明细（含依据核对结果）
     */
    private List<SettlementDiagnosisVO> diagnoses;

    /**
     * 清单手术操作明细（含依据核对结果）
     */
    private List<SettlementOperationVO> operations;

    /**
     * 命中明细条数（items 中的 result=1 数量，便于前端直接展示）
     */
    private Integer hitItemCount;
}
