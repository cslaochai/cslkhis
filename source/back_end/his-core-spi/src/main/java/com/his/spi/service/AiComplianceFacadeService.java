package com.his.spi.service;

import com.his.spi.vo.ComplianceAuditBriefVO;
import com.his.spi.vo.ComplianceEvidenceNarrativeVO;

/**
 * 收费域医保合规审核的对外端口：把一次审核的结论与证据依据交出去，供跨域做证据判定。
 *
 * <p>只读，不写库、不改规则结论；规则怎么命中、依据缺哪些由审核服务判断，调用方不重复这套判定。
 */
public interface AiComplianceFacadeService {

    /**
     * 按审核记录取审核结论与逐规则判定明细。
     *
     * <p>用于医保证据判定前先筛出命中的风险规则；没有命中任何规则时调用方自己决定是拒绝还是留痕。
     *
     * @param auditId 审核记录ID
     * @return 审核摘要；审核记录不存在时抛出业务异常
     */
    ComplianceAuditBriefVO getAuditDetail(Long auditId);

    /**
     * 按审核记录取证据叙事包。
     *
     * <p>用于把清单诊断、病历叙述、诊疗项目与检验结果压成事实文本块，供模型逐条比对证据。
     *
     * @param auditId 审核记录ID
     * @return 证据叙事包；审核记录不存在时抛出业务异常
     */
    ComplianceEvidenceNarrativeVO getAiEvidenceNarrative(Long auditId);
}
