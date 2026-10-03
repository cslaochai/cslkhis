package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品库存流水分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugStockLogQueryPageDTO extends PageParam {
    /**
     * 药品名称（模糊）
     */
    private String drugName;
    /** 变动类型（1-入库 2-发药出库 3-退药回库 4-其他出库 5-盘盈 6-盘亏） */
    private Integer changeType;
}
