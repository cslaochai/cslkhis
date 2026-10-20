package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 药品主数据摘要：跨模块做合理用药判定与剂量换算时要用的那几列。
 */
@Data
@NoArgsConstructor
public class DrugBriefVO {

    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 药品编码
     */
    private String drugCode;

    /**
     * 药品名称
     */
    private String drugName;

    /**
     * 通用名
     */
    private String genericName;

    /**
     * 药品类型
     */
    private Integer drugType;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 每单位含克数（剂量换算用）
     */
    private BigDecimal gramPerUnit;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 是否皮试（1-是 0-否）
     */
    private Integer isSkinTest;

    /**
     * 禁忌症文本
     */
    private String contraindication;
}
