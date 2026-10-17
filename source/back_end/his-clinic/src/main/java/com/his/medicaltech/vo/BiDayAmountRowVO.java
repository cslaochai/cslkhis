package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 近 7 日收入趋势点（BiMapper#revenueTrend 一行）。
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