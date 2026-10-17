package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材库存分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableStockQueryPageDTO extends PageParam {
    /**
     * 关键字（耗材名/编码模糊）
     */
    private String keyword;
    /**
     * 类别
     */
    private Integer category;
    /**
     * 库存状态（1-正常 2-预警 3-缺货 4-过期）
     */
    private Integer stockStatus;
}
