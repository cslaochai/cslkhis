package com.his.supplies.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 耗材出入库流水分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableStockLogQueryPageDTO extends PageParam {
    /** 关键字（耗材名模糊） */
    private String keyword;
    /** 变动类型（1-入库 2-领用出库 3-退回入库 4-其他出库 5-盘盈 6-盘亏） */
    private Integer changeType;
}
