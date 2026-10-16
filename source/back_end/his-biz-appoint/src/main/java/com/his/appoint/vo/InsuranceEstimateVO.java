package com.his.appoint.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保预估结果VO
 */
@Data
public class InsuranceEstimateVO {
    /**
     * 医保类型
     */
    private String insuranceType;
    /**
     * 医保卡号
     */
    private String insuranceNo;
    /**
     * 本单结算方式（1-全自费 2-医保联网）
     */
    private Integer settlementType;
    /**
     * 费别/参保类型（自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）
     */
    private String medicalInsuranceType;
    /**
     * 统筹比例
     */
    private BigDecimal coverageRatio;
    /**
     * 药品总费用，单位：元
     */
    private BigDecimal drugTotal;
    /**
     * 检查费用，单位：元
     */
    private BigDecimal inspectionTotal;
    /**
     * 检验费用，单位：元
     */
    private BigDecimal laboratoryTotal;
    /**
     * 本单总费用，单位：元
     */
    private BigDecimal totalFee;
    /**
     * 统筹支付金额，单位：元
     */
    private BigDecimal insurancePay;
    /**
     * 个人自付金额，单位：元
     */
    private BigDecimal selfPay;
}
