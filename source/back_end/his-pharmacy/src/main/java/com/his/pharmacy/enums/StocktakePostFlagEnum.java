package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 盘点明细过账标记枚举（码值口径 = biz_stocktake_item.posted 列注释）。
 */
@Getter
public enum StocktakePostFlagEnum {

    NONE(0, "未过账"),
    POSTED(1, "已盘盈亏过账"),
    NO_DIFF(2, "无差异免过账");

    private final int code;
    private final String label;

    StocktakePostFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
