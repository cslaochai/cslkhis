package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品供应商退货单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierReturnQueryPageDTO extends PageParam {

    /** 退货单号（模糊） */
    private String returnNo;

    /** 供应商ID */
    private Long supplierId;

    /** 状态（1-待退货 2-已退货 3-已作废） */
    private Integer status;

    /** 退货原因/原单据号关键字（模糊） */
    private String keyword;

    /** 制单日期起（yyyy-MM-dd，按自然日，含当天） */
    private String dateStart;

    /** 制单日期止（yyyy-MM-dd，按自然日，含当天） */
    private String dateEnd;
}
