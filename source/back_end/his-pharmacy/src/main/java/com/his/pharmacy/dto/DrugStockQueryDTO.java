package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品库存分页/预警查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugStockQueryDTO extends PageParam {
    /**
     * 药品名称（模糊查询）
     */
    private String drugName;
    /**
     * 库存状态：1-正常 2-预警 3-缺货 4-过期
     */
    private Integer stockStatus;
    /**
     * 库存地点：1-药库 2-药房（NULL=两层都看，sql/154）
     */
    private Integer stockRoom;
    /**
     * 批次候选是否只出「已挂供应商档案」的批次（供应商退货用，sql/154）
     */
    private Boolean onlyWithSupplier;
}
