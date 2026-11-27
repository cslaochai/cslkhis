package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 近 30 日门诊/住院收入拆分一行（BiMapper#windowRevenue30d）。
 */
@Data
public class BiWindowRevenueRowVO implements Serializable {

    /** 门诊收入净额（元） */
    private BigDecimal outpatient;

    /** 住院收入净额（元） */
    private BigDecimal inpatient;
}
