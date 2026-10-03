package com.his.ai.service;

import com.his.ai.dto.AiAuditLogQueryPageDTO;
import com.his.ai.vo.AiAuditLogVO;
import com.his.common.base.PageResult;

public interface AiAuditQueryService {

    PageResult<AiAuditLogVO> listPage(AiAuditLogQueryPageDTO dto);
}
