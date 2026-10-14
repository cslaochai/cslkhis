package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.RecoverHandleDTO;
import com.mis.dto.RecoverQueryDTO;
import com.mis.entity.MssRecover;
import com.mis.vo.ActionVO;

/**
 * 违规追回
 */
public interface RecoverService {

    PageVO<MssRecover> recoverPage(RecoverQueryDTO query);

    MssRecover getDetail(Long id);

    /** 确认追回：待追回 → 已追回 */
    ActionVO doRecover(RecoverHandleDTO dto);

    /** 核销：待追回 → 已核销 */
    ActionVO writeOff(RecoverHandleDTO dto);
}
