package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierQueryPageDTO extends PageParam {

    /** 关键字：命中编码 / 名称 / 联系人 */
    private String keyword;

    /** 状态：0-停用 1-正常（不传=全部） */
    private Integer status;
}
