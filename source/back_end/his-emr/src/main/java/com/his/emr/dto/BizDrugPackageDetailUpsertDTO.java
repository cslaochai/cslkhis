package com.his.emr.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品套餐明细入参
 */
@Data
public class BizDrugPackageDetailUpsertDTO {
    /**
     * 明细ID，新增时为空
     */
    private Long id;

    /**
     * 所属药品套餐ID
     */
    private Long packageId;

    /**
     * 项目类型：1-药品 2-检查项目 3-检验项目
     */
    private Integer itemType;

    /**
     * 项目ID
     */
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
     * 规格
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价，单位：元
     */
    private BigDecimal price;

    /**
     * 用法用量
     */
    private String usageDosage;

    /** 用药频次 */
    private String frequency;

    /**
     * 给药途径
     */
    private String route;

    /**
     * 用药天数
     */
    private Integer duration;
}
