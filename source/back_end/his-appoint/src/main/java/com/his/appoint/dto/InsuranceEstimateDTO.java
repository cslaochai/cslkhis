package com.his.appoint.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保预估请求DTO
 */
@Data
public class InsuranceEstimateDTO {
    /**
     * 结算方式
     */
    private Integer settlementType;
    /**
     * 医保类型
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
