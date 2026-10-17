package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 在院欠费患者榜查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArrearsBoardQueryDTO extends PageParam {

    /**
     * 患者姓名/患者号模糊
     */
    private String keyword;
}
