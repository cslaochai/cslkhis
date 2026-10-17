package com.his.common.enums;

import lombok.Getter;

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
        if (code == null) {
            return null;
        }
        for (StocktakeDrugTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。脏数据返回 null，由调用侧决定兜底，不回落到合法文案。
     */
    public static String getText(Integer code) {
        StocktakeDrugTypeEnum item = fromCode(code);
        return item == null ? null : item.label;
    }
}
