package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入库单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugInboundQueryPageDTO extends PageParam {

    /** 入库单号（唯一） */
    private String inboundNo;

    /** 入库类型（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库） */
    private Integer inboundType;

    /** 入库状态（1-待审核 2-已审核 3-已入库 4-已取消） */
    private Integer inboundStatus;

    /** 来源采购订单号（模糊） */
    private String purchaseOrderNo;

    /** 制单日期起（yyyy-MM-dd，按自然日，含当天） */
    private String dateStart;

    /** 制单日期止（yyyy-MM-dd，按自然日，含当天） */
    private String dateEnd;
}
