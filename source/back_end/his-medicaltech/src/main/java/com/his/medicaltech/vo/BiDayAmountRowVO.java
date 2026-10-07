package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 近 7 日收入趋势点（{@code BiMapper#revenueTrend} 一行）。
 *
 * <p>金额是记账行净额（收正退负一起 SUM），口径与"今日收入"同源 —— 不按收费单状态反推，
 * 否则"昨天收、今天退"的那笔会被漏掉。
 */
@Data
public class BiDayAmountRowVO implements Serializable {

    /**
     * 记账日（MM-dd）
     */
    private String statDate;

    /**
     * 当日收入净额
     */
    private BigDecimal amount;
}