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
     * 当日收入净额（门诊+住院）
     */
    private BigDecimal amount;

    /**
     * 当日门诊收入净额（encounter_type=1）
     */
    private BigDecimal outpatientAmount;

    /**
     * 当日住院收入净额（encounter_type=2）
     */
    private BigDecimal inpatientAmount;
}