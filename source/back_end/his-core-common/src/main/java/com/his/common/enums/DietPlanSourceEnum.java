package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 膳食方案来源枚举
 */
@Getter
public enum DietPlanSourceEnum {

    ORDER_DERIVED(1, "医嘱校对派生"),
    NUTRITIONIST(2, "营养师手工登记");

    private final int code;
    private final String label;

    DietPlanSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DietPlanSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DietPlanSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DietPlanSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DietPlanSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DietPlanSourceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
