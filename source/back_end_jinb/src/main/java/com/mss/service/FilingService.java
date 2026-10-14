package com.mss.service;

import com.mss.common.base.PageVO;
import com.mss.dto.FilingQueryDTO;
import com.mss.dto.FilingUpsertDTO;
import com.mss.entity.MssFiling;
import com.mss.vo.ActionVO;

/**
 * 异地就医备案
 */
public interface FilingService {

    PageVO<MssFiling> filingPage(FilingQueryDTO query);

    ActionVO upsert(FilingUpsertDTO dto);

    ActionVO revoke(Long id);
}
