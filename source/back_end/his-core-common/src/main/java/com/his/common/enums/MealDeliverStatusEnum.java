package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配餐状态枚举
 */
@Getter
public enum MealDeliverStatusEnum {

    PENDING(0, "待配餐"),
    PREPARED(1, "已配餐"),
    DELIVERED(2, "已配送"),
    SIGNED(3, "已签收"),
    CANCELED(4, "已取消");

    private final int code;
    private final String label;

    MealDeliverStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MealDeliverStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (MealDeliverStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        MealDeliverStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        MealDeliverStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        MealDeliverStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
