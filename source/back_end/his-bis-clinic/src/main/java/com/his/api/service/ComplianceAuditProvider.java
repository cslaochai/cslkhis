package com.his.api.service;

import com.his.api.vo.ComplianceAuditDetailVO;
import com.his.api.vo.ComplianceEvidenceNarrativeVO;

/**
 * 医保合规审核读侧契约（his-charge 实现）。
 */
public interface ComplianceAuditProvider {

    /**
     * 查询审核详情（含全部规则判定明细）
     */
    ComplianceAuditDetailVO getAuditDetail(Long auditId);

    /**
     * 证据叙事包：把一次审核的依据包压平成模型可读的事实文本块。
     * 只读产物，供 his-ai 的医保证据判定能力拼提示词；不写库、不改规则结论。
     */
    ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId);
}
