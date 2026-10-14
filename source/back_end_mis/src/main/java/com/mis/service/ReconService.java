package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.BizPageParam;
import com.mis.entity.MssRecon;
import com.mis.vo.ActionVO;

/**
 * 清算对账
 */
public interface ReconService {

    PageVO<MssRecon> reconPage(BizPageParam query);

    ActionVO doReconcile(String period);
}
