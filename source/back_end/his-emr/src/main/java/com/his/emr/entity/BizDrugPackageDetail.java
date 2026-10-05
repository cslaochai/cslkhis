package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 药品耗材套餐明细（药品耗材套餐明细）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_package_detail")
public class BizDrugPackageDetail extends BaseEntity {

    /**
     * 套餐ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packageId;
    /**
     * 项目类型（1-药品 2-检查 3-检验）
     */
    private Integer itemType;
    /**
     * 项目ID
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
     * 单价
     */
    private BigDecimal price;
    /**
     * 用法用量
     */
    private String usageDosage;
    /**
     * 用药频次
     */
    private String frequency;
    /**
     * 用药途径
     */
    private String route;
    /**
     * 疗程天数
     */
    private Integer duration;
}
