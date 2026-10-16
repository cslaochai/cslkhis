package com.his.appoint.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保预估请求DTO
 */
@Data
public class InsuranceEstimateDTO {
    /**
     * 本单结算方式（1-全自费 2-医保联网）
     */
    private Integer settlementType;
    /**
     * 费别/参保类型（自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）
     */
    private String medicalInsuranceType;
    /**
     * 药品总费用
     */
    private BigDecimal drugTotal;
    /**
     * 检查费用
     */
    private BigDecimal inspectionTotal;
    /**
     * 检验费用
     */
    private BigDecimal laboratoryTotal;
}
