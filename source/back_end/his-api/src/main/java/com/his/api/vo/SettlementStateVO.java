package com.his.api.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算状态。
 */
@Data
@NoArgsConstructor
public class SettlementStateVO implements Serializable {

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