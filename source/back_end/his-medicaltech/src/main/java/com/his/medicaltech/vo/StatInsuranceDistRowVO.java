package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 险种构成（StatReportAggMapper#insuranceDist 一行）。
 */
@Data
public class StatInsuranceDistRowVO implements Serializable {

    /**
     * 险种名称（未登记时为「未登记」）
     */
    private String insuranceType;

    /**
     * 该险种结算单笔数
     */
    private Long cnt;

    /**
     * 该险种费用总额
     */
    private BigDecimal amount;
}