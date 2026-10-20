package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 盘点范围的药品类别口径（1-西药 2-中成药 3-中药饮片）。
 *
 * <p>与 {@code com.his.common.enums.PaymentItemTypeEnum}（结算项目类型：2-西药 3-中成药 4-中药饮片）
 * 码值错位，严禁互相套用。
 */
@Getter
public enum StocktakeDrugTypeEnum {

    WESTERN(1, "西药"),
    PATENT(2, "中成药"),
    HERBAL(3, "中药饮片");

    private final int code;
    private final String label;

    StocktakeDrugTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StocktakeDrugTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StocktakeDrugTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        StocktakeDrugTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StocktakeDrugTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
