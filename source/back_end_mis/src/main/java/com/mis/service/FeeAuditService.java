package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.AuditQueryDTO;
import com.mis.entity.MssFeeAudit;
import com.mis.vo.ActionVO;

/**
 * 费用合规性审核
 */
public interface FeeAuditService {

    PageVO<MssFeeAudit> auditPage(AuditQueryDTO query);

    ActionVO pass(Long id);

    ActionVO reject(Long id);
}
