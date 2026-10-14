package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.RuleQueryDTO;
import com.mis.dto.RuleStatusDTO;
import com.mis.dto.RuleUpsertDTO;
import com.mis.entity.MssAuditRule;
import com.mis.vo.ActionVO;

import java.util.List;

/**
 * 监管规则
 */
public interface AuditRuleService {

    PageVO<MssAuditRule> rulePage(RuleQueryDTO query);

    ActionVO upsert(RuleUpsertDTO dto);

    ActionVO changeStatus(RuleStatusDTO dto);

    List<MssAuditRule> enabledRules();
}
