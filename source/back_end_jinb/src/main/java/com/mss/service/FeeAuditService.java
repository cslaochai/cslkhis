package com.mss.service;

import com.mss.common.base.PageVO;
import com.mss.dto.AuditQueryDTO;
import com.mss.entity.MssFeeAudit;
import com.mss.vo.ActionVO;

/**
 * 费用合规性审核
 */
public interface FeeAuditService {

    PageVO<MssFeeAudit> auditPage(AuditQueryDTO query);

    ActionVO pass(Long id);

    ActionVO reject(Long id);
}
