package com.his.system.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ICD-10智能预测出参
 */
@Data
public class Icd10PredictVO {

    /**
     * ICD-10编码
     */
    private String icdCode;

    /**
     * ICD-10名称
     */
    private String icdName;

    /**
     * ICD分类
     */
    private String icdCategory;

    /**
     * 匹配得分，越高越相关
     */
    private int score;

    /**
     * DRG权重
     */
    private BigDecimal drgWeight;

    /**
     * 预估费用，单位：元
     */
    private BigDecimal estimatedCost;
}
