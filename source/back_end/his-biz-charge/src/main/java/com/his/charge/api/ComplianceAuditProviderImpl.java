package com.his.charge.api;

import com.his.api.service.ComplianceAuditProvider;
import com.his.api.vo.ComplianceAuditDetailVO;
import com.his.api.vo.ComplianceEvidenceNarrativeVO;
import com.his.charge.service.ComplianceAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ComplianceAuditApi 在 his-charge 侧的实现（读侧直通，不加逻辑）。
 */
@Service
@RequiredArgsConstructor
public class ComplianceAuditProviderImpl implements ComplianceAuditProvider {

    private final ComplianceAuditService complianceAuditService;

    @Override
    public ComplianceAuditDetailVO getAuditDetail(Long auditId) {
        return complianceAuditService.getAuditDetail(auditId);
    }

    @Override
    public ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId) {
        return complianceAuditService.getAiEvidenceNarrative(auditId);
    }
}
