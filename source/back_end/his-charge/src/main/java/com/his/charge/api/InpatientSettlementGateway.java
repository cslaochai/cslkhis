package com.his.charge.api;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 住院结算状态扩展点（由 his-charge 模块提供实现）。
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
