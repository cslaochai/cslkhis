package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 近 30 日医保结算汇总一行（BiMapper#insuranceSettle30d）。
 */
@Data
public class BiInsuranceRowVO implements Serializable {

    /** 医保支付金额（统筹 + 个账，元） */
    private BigDecimal insurance;

    /** 结算费用总额（元） */
    private BigDecimal total;
}
