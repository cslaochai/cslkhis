package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DRG 组权重一行（BiMapper#drgWeights）。
 */
@Data
public class BiDrgWeightRowVO implements Serializable {

    /**
     * DRG 组编码
     */
    private String drgCode;

    /**
     * 组权重
     */
    private BigDecimal weight;
}