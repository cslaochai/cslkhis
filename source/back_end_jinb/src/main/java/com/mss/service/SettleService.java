package com.mss.service;

import com.mss.common.base.PageVO;
import com.mss.dto.BillQueryDTO;
import com.mss.dto.BizPageParam;
import com.mss.vo.BillVO;
import com.mss.vo.DecVO;
import com.mss.vo.PendingSettleVO;
import com.mss.vo.SettleResultVO;

/**
 * 经办即时结算：门诊/住院待结算、试算、执行、结算单查询、退费冲正
 */
public interface SettleService {

    /** 待结算业务分页（bizType 1-门诊 2-住院） */
    PageVO<PendingSettleVO> pendingPage(int bizType, BizPageParam query);

    /** 费用分解试算（不落库） */
    DecVO preview(String bizNo);

    /** 执行结算（bizType 1-门诊 2-住院），生成结算单 */
    SettleResultVO confirm(int bizType, String bizNo);

    /** 结算单分页 */
    PageVO<BillVO> billPage(BillQueryDTO query);

    /** 结算单明细 */
    BillVO billDetail(Long id);

    /** 退费冲正 */
    SettleResultVO reverse(Long id);
}
