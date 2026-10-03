package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品调拨单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrugTransferQueryPageDTO extends PageParam {

    /** 调拨单号（模糊） */
    private String transferNo;

    /** 方向（1-药库下拨药房 2-药房退回药库） */
    private Integer transferType;

    /** 状态（1-待发出 2-待接收 3-已完成 4-已作废） */
    private Integer status;

    /** 事由/单号关键字（模糊） */
    private String keyword;

    /** 制单日期起（yyyy-MM-dd，按自然日，含当天） */
    private String dateStart;

    /** 制单日期止（yyyy-MM-dd，按自然日，含当天） */
    private String dateEnd;
}
