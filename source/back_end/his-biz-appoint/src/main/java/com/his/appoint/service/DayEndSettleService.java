package com.his.appoint.service;

import com.his.appoint.dto.DayEndSettleDTO;
import com.his.appoint.vo.DayEndSettleResultVO;

/**
 * 日终结转 —— 给「昨天的号」收尾。
 */
public interface DayEndSettleService {

    /**
     * 单次补跑的最大天数（安全阀）
     */
    int MAX_BACKFILL_DAYS = 31;

    /**
     * 结转某一天。{@code settleDate} 为空按 DTO 里的语义由实现决定。
     *
     * @throws com.his.common.exception.BusinessException 当传入的不是过去日期（当天还在营业，不能结转）
     */
    DayEndSettleResultVO settle(DayEndSettleDTO settleDTO);

    /**
     * 补跑「所有还留着遗留记录的历史日期」直到昨天。
     *
     * <p>由 {@code DayEndSettleTrigger} 在门诊相关页面访问时顺手调用，也用于系统停摆几天后的追补。
     * 单次最多回补 {@link #MAX_BACKFILL_DAYS} 天，避免一个空库被拉到几年前。
     */
    DayEndSettleResultVO settlePending(boolean dryRun);
}
