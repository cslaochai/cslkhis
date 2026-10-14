package com.mis.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 医保费用分解（试算）结果
 */
@Data
public class DecVO implements Serializable {

    private BigDecimal total;
    private BigDecimal insurancePay;
    private BigDecimal personalAccount;
    private BigDecimal cashPay;
    private BigDecimal selfPay;
    private BigDecimal deductible;
    private BigDecimal basePool;
}
