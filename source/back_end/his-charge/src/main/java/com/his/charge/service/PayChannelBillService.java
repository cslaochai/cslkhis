package com.his.charge.service;


import com.his.charge.dto.*;
import com.his.charge.vo.PayChannelBillVO;
import com.his.charge.vo.PayChannelCandidateVO;
import com.his.charge.vo.PayChannelSummaryVO;
import com.his.common.base.PageResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 支付渠道对账服务（M7 留口子，四层口径：勾支付资金流水的流水编号）。
 */
public interface PayChannelBillService {

    /**
     * 分页查询渠道流水台账
     */
    PageResult<PayChannelBillVO> selectPage(PayChannelQueryPageDTO query);

    /**
     * 拉取渠道账单（当前为控制台打印桩）并落台账（幂等）
     */
    int importBill(PayChannelImportDTO dto);

    /**
     * 手工登记渠道侧流水
     */
    boolean manualRegister(PayChannelManualDTO dto);

    /**
     * 人工勾对：渠道流水 → 本地支付流水（金额不符直接拒绝）
     */
    boolean match(PayChannelMatchDTO dto);

    /**
     * 长款/短款处理（定性留痕）
     */
    boolean handleDiff(PayChannelDiffDTO dto);

    /**
     * 渠道对账汇总（本地口径 vs 渠道口径）
     */
    PayChannelSummaryVO summary(LocalDate billDate);

    /**
     * 勾对候选：当日该渠道可勾对的本地成功支付流水（未被他笔台账占用，金额相等优先）
     */
    List<PayChannelCandidateVO> matchCandidates(Long channelBillId);
}
