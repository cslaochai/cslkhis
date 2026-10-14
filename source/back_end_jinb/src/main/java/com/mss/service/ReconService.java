package com.mss.service;

import com.mss.common.base.PageVO;
import com.mss.dto.BizPageParam;
import com.mss.entity.MssRecon;
import com.mss.vo.ActionVO;

/**
 * 清算对账
 */
public interface ReconService {

    PageVO<MssRecon> reconPage(BizPageParam query);

    ActionVO doReconcile(String period);
}
