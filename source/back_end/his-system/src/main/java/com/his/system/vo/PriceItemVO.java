package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格项目出参（跨价表统一结构）
 */
@Data
public class PriceItemVO {

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗
     */
    private String itemType;

    /**
     * 项目ID（雪花ID，序列化为字符串）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格（仅药品、耗材有）
     */
    private String specification;

    /**
     * 单位（仅药品、耗材、检验有）
     */
    private String unit;

    /**
     * 生产厂家（仅药品、耗材有）
     */
    private String manufacturer;

    /**
     * 当前价格
     */
    private BigDecimal price;

    /**
     * 成本价（仅药品有）
     */
    private BigDecimal costPrice;

    /**
     * 是否医保（0-否 1-是，仅药品有）
     */
    private Integer medicalInsurance;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
