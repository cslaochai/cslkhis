package com.his.appoint.service;

import com.his.appoint.dto.DayEndSettleDTO;
import com.his.appoint.vo.DayEndSettleResultVO;

/**
 * 日终结转 —— 给「昨天的号」收尾。
 *
 * <p><b>为什么必须有这一步</b>：门诊的每一次挂号都该有结局。真实数据里，
 * 「昨天挂了号没来」会永久停在「已挂号」，「昨天签到了没看上」会永久停在「已签到」
 * 加一条「候诊中」的队列行。于是每个「按今天筛」的查询都得各自兜一遍遗留，
 * 兜漏了就是「患者昨天来过、今天面板上还挂着」这类静默错误（本项目已经踩过两次：
 * 跨日签到、复诊超时）。日终结转把这些收口成一次，之后「昨天」再也不会出现在
 * 「今天」的口径里。
 *
 * <p>三个终态：
 * <ul>
 *   <li>未签到（{@code regist_status=1}）→ 7 爽约；</li>
 *   <li>已签到/已接诊未就诊（2/3）→ 8 未就诊；</li>
 *   <li>队列行（候诊中 2 / 就诊中 3）→ 7 已失效。</li>
 * </ul>
 * <b>不动号源池</b>：昨天的号不会变回可卖的，减 used_source 只会让历史报表失真（理由见
 * {@code DayEndSettleMapper} 类注释）。
 *
 * <p><b>可重入</b>：结转只认「还停在中间态」的行，跑第二遍影响 0 条；
 * 已经进/退号的患者不会被二次改写，所以可以放心地自动 + 手工重复触发。
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
