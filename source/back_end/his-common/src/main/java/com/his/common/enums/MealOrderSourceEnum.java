package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 订餐来源枚举
 */
@Getter
public enum MealOrderSourceEnum {

    GENERATE(1, "按膳食方案批量生成"),
    MANUAL(2, "手工加订");

    private final int code;
    private final String label;

    MealOrderSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MealOrderSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (MealOrderSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        MealOrderSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        MealOrderSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        MealOrderSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
