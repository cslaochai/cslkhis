package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DRG 组权重一行（{@code BiMapper#drgWeights}）。
 *
 * <p>组表一次查全后内存建 code → weight 映射：CMI 要逐条分组，
 * 每条再查一次组表就是 N+1 次数据库往返。
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