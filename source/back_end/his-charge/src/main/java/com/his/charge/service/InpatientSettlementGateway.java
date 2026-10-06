package com.his.charge.service;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院结算状态扩展点（由 his-charge 模块提供实现）。
 *
 * <p>为什么又是 SPI：依赖方向是 {@code his-charge → his-patient}（单向），
 * 出院办理住在 his-patient，若反向依赖 his-charge 会成环、Maven 直接构建失败。
 * 所以接口放调用方，实现放被依赖方。
 *
 * <p><b>出院前必须已结算（结清或欠费结算都算）</b>：一次住院没有结算单就出院，
 * 财务侧就永远追不到这笔账 —— 这不是"提醒"，是拦截。
 *
 * <p>实现侧的事实来源是 L2 账单（结算账单里 bill_type=4-出院结算
 * 且未作废的那张），不再读旧表住院结算单：一次结算在两层各记一份，
 * 两份必然漂移，出院门禁就会读到过期那一份。
 *
 * <p>调用侧必须用 {@code ObjectProvider<...>.getIfAvailable()} 惰性获取：
 * 收费模块缺席时**放行但在出院记录里写明"未校验结算"**，绝不假装校验过。
 */
public interface InpatientSettlementGateway {

    /**
     * 查询某次住院的结算状态。
     *
     * @param admissionId 入院ID
     * @return 结算状态；没有任何结算单时返回 {@code settled=false} 的状态对象
     */
    SettlementState state(Long admissionId);

    /**
     * 结算状态。
     */
    @Data
    class SettlementState implements Serializable {

        /**
         * 是否已办理结算（结清 / 欠费结算都算已结算）
         */
        private boolean settled;

        /**
         * 结算状态：1-已结清 2-欠费 3-已作废
         */
        private Integer settleStatus;

        /**
         * 结算单号
         */
        private String settlementNo;

        /**
         * 欠费金额（元）
         */
        private BigDecimal arrearsAmount;

        /**
         * 一句话状态（供出院记录留痕与前端展示，前端不自己拼文案）
         */
        private String text;
    }
}
