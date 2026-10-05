package com.his.charge.service;

import com.his.charge.dto.ComplianceAuditQueryPageDTO;
import com.his.charge.dto.ComplianceBatchAuditDTO;
import com.his.charge.dto.SettlementCodingUpsertDTO;
import com.his.charge.vo.ComplianceAuditDetailVO;
import com.his.charge.vo.ComplianceAuditVO;
import com.his.charge.vo.ComplianceEvidenceNarrativeVO;
import com.his.charge.vo.SettlementCodingVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 医保合规审核服务（防止高编高套 / 低编入组）
 */
public interface ComplianceAuditService {

    /**
     * 取结算清单的编码明细（诊断 + 手术操作）
     */
    SettlementCodingVO getCoding(Long settlementId);

    /**
     * 整单覆盖保存编码明细
     */
    void saveCoding(SettlementCodingUpsertDTO dto);

    /**
     * 清空该清单的编码明细（诊断 + 手术操作）
     */
    void clearCoding(Long settlementId);

    /**
     * 单张清单合规自查（结算前自查 / 医保反馈复核）
     */
    ComplianceAuditDetailVO audit(Long settlementId, Integer auditType);

    /**
     * 批量筛查，逐个出具审核记录
     */
    List<ComplianceAuditDetailVO> batchAudit(ComplianceBatchAuditDTO dto);

    /**
     * 分页查询审核记录
     */
    PageResult<ComplianceAuditVO> selectAuditPage(ComplianceAuditQueryPageDTO queryDTO);

    /**
     * 查询审核详情（含全部规则判定明细）
     */
    ComplianceAuditDetailVO getAuditDetail(Long auditId);

    /**
     * 证据叙事包：把一次审核的依据包压平成模型可读的事实文本块（G-07）。
     * 只读产物，供 his-ai 的医保证据判定能力拼提示词；不写库、不改规则结论。
     */
    ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId);
}
