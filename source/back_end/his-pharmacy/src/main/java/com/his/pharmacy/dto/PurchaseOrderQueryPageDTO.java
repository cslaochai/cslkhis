package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购订单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderQueryPageDTO extends PageParam {

    /** 订单号（模糊） */
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 审批状态（0-待审批 1-已通过 2-已驳回） */
    private Integer approvalStatus;

    /** 是否已入库（true=已入库 / false=未入库；不传=全部）。派生自入库单，不是订单表的列 */
    private Boolean inboundDone;

    /** 下单日期起（yyyy-MM-dd，按自然日，含当天） */
    private String dateStart;

    /** 下单日期止（yyyy-MM-dd，按自然日，含当天） */
    private String dateEnd;
}
