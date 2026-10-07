package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 「笔数 + 金额」聚合行：日结/班结里所有<b>单值</b>聚合的共同形状。
 *
 * <p>对应 SQL 形态固定为 {@code SELECT COUNT(*) AS cnt, COALESCE(SUM(x), 0) AS amount ...}，
 * 当前挂这 6 条聚合（口径各不相同，切勿混用）：
 * <ul>
 *   <li>{@code BizDaySettlementMapper#sumRefund} —— 当日退款笔数 + 退款合计（取绝对值）</li>
 *   <li>{@code BizDaySettlementMapper#sumUnassigned} —— 未纳入任何交班单的收款笔数 + 金额</li>
 *   <li>{@code BizDaySettlementMapper#sumSystemCollected} —— 系统代收（{@code cashier_id=0}）笔数 + 金额</li>
 *   <li>{@code BizDaySettlementMapper#sumBillHeader} —— 当日收讫账单的单头应收笔数 + 合计</li>
 *   <li>{@code BizDaySettlementMapper#sumDetailUnattributed} / {@code #sumDetailAll} —— 摊行笔数 + 金额</li>
 *   <li>{@code BizCashierSettlementMapper#sumRefundBySettlement} —— 本班退费笔数 + 金额</li>
 * </ul>
 *
 * <p><b>为什么共用一个类</b>：这 6 条 SQL 的输出列完全同构（都是"多少笔、共多少钱"），
 * 差异只在聚合哪张表、按什么口径 —— 那是 SQL 与调用点该写清的事，不是数据结构该分的。
 * 为它们建 6 个字段一模一样的类只会让人误以为它们语义不同。
 *
 * <p><b>为什么金额是 BigDecimal 而不是 Long</b>：{@code COALESCE(SUM(amount), 0)} 里的
 * {@code amount} 列是 {@code DECIMAL}，MySQL 对 DECIMAL 求和返回 DECIMAL，MyBatis 按目标字段类型
 * 取值。原来走裸 {@code Map} 时靠 {@code new BigDecimal(String.valueOf(v))} 兜住类型，
 * 换成VO 后由 MyBatis 直接映射到 BigDecimal，精度一分不丢（<b>不</b>经过 double）。
 */
@Data
public class CountAmountVO implements Serializable {

    /**
     * 笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 金额合计（元）。SQL 已用 {@code COALESCE(..., 0)} 兜底，不会为 null
     */
    private BigDecimal amount;
}