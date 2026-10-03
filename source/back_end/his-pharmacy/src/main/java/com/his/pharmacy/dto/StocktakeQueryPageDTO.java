package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点单分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StocktakeQueryPageDTO extends PageParam {

    /** 盘点单号（模糊） */
    private String stocktakeNo;

    /** 盘点主题（模糊） */
    private String stocktakeTitle;

    /** 状态（1-盘点中 2-待复核 3-已过账 4-已关单） */
    private Integer status;

    /** 制单日期起（yyyy-MM-dd，按自然日，含当天） */
    private String dateStart;

    /** 制单日期止（yyyy-MM-dd，按自然日，含当天） */
    private String dateEnd;
}
