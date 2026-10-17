package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保结算简要（读侧契约）。
 */
@Data
public class InsuranceBriefVO {

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 报销比例（%）
     */
    private BigDecimal coverageRatio;
}
